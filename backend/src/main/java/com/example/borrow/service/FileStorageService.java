package com.example.borrow.service;

import com.example.borrow.entity.Attachment;
import com.example.borrow.entity.OperationLog;
import com.example.borrow.repository.AttachmentRepository;
import com.example.borrow.repository.OperationLogRepository;
import com.example.borrow.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * 附件存储服务：文件落本地目录，元数据入库。
 * 目录可用 application.yml 的 app.upload-dir 配置（默认 ./uploads）。
 */
@Service
public class FileStorageService {

    @Autowired private AttachmentRepository attachmentRepository;
    @Autowired private OperationLogRepository operationLogRepository;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    private Path root;

    @PostConstruct
    public void init() {
        try {
            root = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("无法创建附件目录：" + e.getMessage());
        }
    }

    public Attachment store(MultipartFile file, String refType, Long refId) {
        if (file == null || file.isEmpty()) throw new RuntimeException("上传文件为空");
        if (refType == null || refType.isEmpty() || refId == null) throw new RuntimeException("缺少关联对象");

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot);
        String stored = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Path target = root.resolve(stored);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }

            Attachment a = new Attachment();
            a.setRefType(refType);
            a.setRefId(refId);
            a.setFileName(original);
            a.setStoredName(stored);
            a.setContentType(file.getContentType());
            a.setFileSize(file.getSize());
            a.setUploader(UserContext.getRealName() != null ? UserContext.getRealName() : UserContext.getUsername());
            a = attachmentRepository.save(a);

            log("ATTACHMENT_UPLOAD", "上传附件「" + original + "」", refId);
            return a;
        } catch (IOException e) {
            throw new RuntimeException("附件保存失败：" + e.getMessage());
        }
    }

    public List<Attachment> list(String refType, Long refId) {
        return attachmentRepository.findByRefTypeAndRefIdOrderByIdDesc(refType, refId);
    }

    public Attachment get(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("附件不存在"));
    }

    public File loadFile(Attachment a) {
        File f = root.resolve(a.getStoredName()).toFile();
        if (!f.exists()) throw new RuntimeException("附件文件已丢失：" + a.getFileName());
        return f;
    }

    public void delete(Long id) {
        Attachment a = get(id);
        File f = root.resolve(a.getStoredName()).toFile();
        if (f.exists()) {
            // 删除失败不影响数据记录清理（例如文件被占用）
            boolean ignored = f.delete();
        }
        attachmentRepository.delete(a);
        log("ATTACHMENT_DELETE", "删除附件「" + a.getFileName() + "」", a.getRefId());
    }

    private void log(String action, String detail, Long targetId) {
        try {
            OperationLog log = new OperationLog();
            log.setUsername(UserContext.getUsername());
            log.setRealName(UserContext.getRealName());
            log.setAction(action);
            log.setDetail(detail);
            log.setTargetType("MATERIAL");
            log.setTargetId(targetId);
            operationLogRepository.save(log);
        } catch (Exception ignored) {}
    }
}

package com.example.borrow.controller;

import com.example.borrow.entity.Attachment;
import com.example.borrow.security.RoleGuard;
import com.example.borrow.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;

/**
 * 附件接口：物资照片 / 说明书 / 入库发票等留证材料。
 * 上传与删除限教师与超管（基础数据维护类操作），查询所有登录用户可用。
 */
@RestController
@RequestMapping("/api/attachment")
public class AttachmentController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping
    public Result<?> upload(@RequestParam("file") MultipartFile file,
                            @RequestParam("refType") String refType,
                            @RequestParam("refId") Long refId) {
        RoleGuard.requireStaff();
        return Result.ok(fileStorageService.store(file, refType, refId));
    }

    @GetMapping
    public Result<?> list(@RequestParam String refType, @RequestParam Long refId) {
        return Result.ok(fileStorageService.list(refType, refId));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        RoleGuard.requireStaff();
        fileStorageService.delete(id);
        return Result.ok();
    }

    /** 下载 / 预览：图片可直接在浏览器中查看 */
    @GetMapping("/{id}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long id) throws Exception {
        Attachment a = fileStorageService.get(id);
        File file = fileStorageService.loadFile(a);
        String name = URLEncoder.encode(a.getFileName() != null ? a.getFileName() : "file", "UTF-8")
                .replace("+", "%20");
        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        if (a.getContentType() != null) {
            try { type = MediaType.parseMediaType(a.getContentType()); } catch (Exception ignored) {}
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + name)
                .contentType(type)
                .body(new FileSystemResource(file));
    }
}

package com.example.borrow.service;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.entity.Material;
import com.example.borrow.entity.OperationLog;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.repository.OperationLogRepository;
import com.example.borrow.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class BorrowService {

    @Autowired private BorrowRecordRepository borrowRecordRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private OperationLogRepository operationLogRepository;
    @Autowired private SystemConfigRepository systemConfigRepository;
    @Autowired private NotificationService notificationService;

    private int getConfigInt(String key, int def) {
        return systemConfigRepository.findByConfigKey(key)
            .map(c -> { try { return Integer.parseInt(c.getConfigValue()); } catch(Exception e){return def;} })
            .orElse(def);
    }

    // ==================== 借用申请（学生 → PENDING） ====================

    @Transactional
    public BorrowRecord requestBorrow(Long materialId, String borrower, Integer quantity,
                                       String purpose, LocalDateTime expectedReturnTime) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("物资不存在"));
        if (material.getAvailableQuantity() < quantity)
            throw new RuntimeException("库存不足，当前可借数量：" + material.getAvailableQuantity());

        BorrowRecord record = new BorrowRecord();
        record.setMaterialId(materialId);
        record.setMaterialName(material.getName());
        record.setMaterialCategory(material.getCategory());
        record.setBorrower(borrower);
        record.setBorrowQuantity(quantity);
        record.setStatus("PENDING");
        record.setBorrowTime(LocalDateTime.now());
        record.setExpectedReturnTime(expectedReturnTime);
        record.setPurpose(purpose);
        record = borrowRecordRepository.save(record);

        log(borrower, "BORROW_REQUEST", "申请借用「" + material.getName() + "」×" + quantity, "BORROW", record.getId());
        return record;
    }

    // ==================== 审批通过（教师） ====================

    @Transactional
    public BorrowRecord approveBorrow(Long recordId, String approver) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"PENDING".equals(record.getStatus()))
            throw new RuntimeException("该记录不是待审批状态");

        Material material = materialRepository.findById(record.getMaterialId())
                .orElseThrow(() -> new RuntimeException("物资不存在"));
        if (material.getAvailableQuantity() < record.getBorrowQuantity())
            throw new RuntimeException("库存不足，无法通过审批");

        material.setAvailableQuantity(material.getAvailableQuantity() - record.getBorrowQuantity());
        materialRepository.save(material);

        record.setStatus("APPROVED");
        record.setApprover(approver);
        record.setApproveTime(LocalDateTime.now());
        record = borrowRecordRepository.save(record);

        log(approver, "APPROVE", "通过「" + record.getBorrower() + "」借用「" + record.getMaterialName() + "」", "BORROW", recordId);
        return record;
    }

    // ==================== 审批拒绝（教师） ====================

    @Transactional
    public void rejectBorrow(Long recordId, String approver, String reason) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"PENDING".equals(record.getStatus()))
            throw new RuntimeException("该记录不是待审批状态");

        record.setStatus("REJECTED");
        record.setApprover(approver);
        record.setApproveTime(LocalDateTime.now());
        record.setRejectReason(reason);
        borrowRecordRepository.save(record);

        notificationService.send(record.getBorrower(),
            "借用申请被拒绝",
            "您借用的「" + record.getMaterialName() + "」申请已被拒绝。" + (reason != null ? "原因：" + reason : ""),
            "SYSTEM");
        log(approver, "REJECT", "拒绝「" + record.getBorrower() + "」借用「" + record.getMaterialName() + "」", "BORROW", recordId);
    }

    // ==================== 申请延期（学生） ====================

    @Transactional
    public void requestExtension(Long recordId, String borrower, int days, String reason) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"BORROWED".equals(record.getStatus()) && !"OVERDUE".equals(record.getStatus()))
            throw new RuntimeException("只有借用中或逾期的记录才能申请延期");

        record.setStatus("PENDING_EXTEND");
        record.setExtensionDays(days);
        record.setExtensionReason(reason);
        borrowRecordRepository.save(record);

        log(borrower, "EXTEND_REQUEST", "申请延期「" + record.getMaterialName() + "」" + days + "天", "BORROW", recordId);
    }

    // ==================== 审批延期（教师） ====================

    @Transactional
    public void approveExtension(Long recordId, String approver) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"PENDING_EXTEND".equals(record.getStatus()))
            throw new RuntimeException("该记录不是待审批延期状态");

        int days = record.getExtensionDays() != null ? record.getExtensionDays() : 7;
        LocalDateTime newReturnTime = (record.getExpectedReturnTime() != null
                ? record.getExpectedReturnTime() : LocalDateTime.now()).plusDays(days);
        record.setExpectedReturnTime(newReturnTime);
        record.setStatus("BORROWED");
        record.setExtensionApproved(true);
        record.setExtensionDays(null);
        borrowRecordRepository.save(record);

        notificationService.send(record.getBorrower(),
            "延期申请已通过",
            "您借用的「" + record.getMaterialName() + "」延期" + days + "天已批准，新归还日期：" + newReturnTime.toLocalDate(),
            "SYSTEM");
        log(approver, "EXTEND_APPROVE", "批准「" + record.getBorrower() + "」延期「" + record.getMaterialName() + "」" + days + "天", "BORROW", recordId);
    }

    @Transactional
    public void rejectExtension(Long recordId, String approver) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"PENDING_EXTEND".equals(record.getStatus()))
            throw new RuntimeException("该记录不是待审批延期状态");

        // 恢复原状态
        LocalDateTime now = LocalDateTime.now();
        if (record.getExpectedReturnTime() != null && record.getExpectedReturnTime().isBefore(now))
            record.setStatus("OVERDUE");
        else
            record.setStatus("BORROWED");
        record.setExtensionDays(null);
        borrowRecordRepository.save(record);

        notificationService.send(record.getBorrower(),
            "延期申请被拒绝",
            "您借用的「" + record.getMaterialName() + "」延期申请已被拒绝，请按时归还。",
            "SYSTEM");
        log(approver, "EXTEND_REJECT", "拒绝「" + record.getBorrower() + "」延期「" + record.getMaterialName() + "」", "BORROW", recordId);
    }

    // ==================== 归还（含损坏 + 罚款） ====================

    @Transactional
    public Map<String, Object> returnMaterial(Long recordId, Integer returnQuantity, String returnRemark,
                                               Boolean isDamaged, String damageDescription) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"BORROWED".equals(record.getStatus()) && !"OVERDUE".equals(record.getStatus()))
            throw new RuntimeException("该记录不是借用中或逾期状态");

        int actualReturn = returnQuantity != null ? returnQuantity : record.getBorrowQuantity();

        // 归还物资
        Material material = materialRepository.findById(record.getMaterialId())
                .orElseThrow(() -> new RuntimeException("物资不存在"));
        material.setAvailableQuantity(material.getAvailableQuantity() + actualReturn);
        materialRepository.save(material);

        // 计算逾期天数和罚款
        int overdueDays = 0;
        double fineAmount = 0;
        if (record.getExpectedReturnTime() != null) {
            LocalDateTime deadline = record.getExpectedReturnTime();
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(deadline)) {
                overdueDays = (int) ChronoUnit.DAYS.between(deadline.toLocalDate(), now.toLocalDate());
                if (overdueDays > 0) {
                    int finePerDay = getConfigInt("overdue_fine_per_day", 0);
                    fineAmount = overdueDays * finePerDay;
                }
            }
        }
        record.setOverdueDays(overdueDays);
        record.setFineAmount(fineAmount);

        // 处理损坏
        if (isDamaged != null && isDamaged) {
            record.setStatus("DAMAGED");
            record.setIsDamaged(true);
            record.setDamageDescription(damageDescription);
        } else {
            record.setStatus("RETURNED");
        }

        record.setActualReturnTime(LocalDateTime.now());
        record.setReturnRemark(returnRemark);
        if (returnQuantity != null) record.setBorrowQuantity(actualReturn);
        record = borrowRecordRepository.save(record);

        String detail = "归还「" + record.getMaterialName() + "」" + (isDamaged != null && isDamaged ? "（有损坏）" : "");
        if (fineAmount > 0) detail += "，逾期" + overdueDays + "天，罚款¥" + fineAmount;
        log(record.getBorrower(), "RETURN", detail, "BORROW", recordId);

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("overdueDays", overdueDays);
        result.put("fineAmount", fineAmount);
        return result;
    }

    // ==================== 列表 / 统计 ====================

    public List<BorrowRecord> list(String status, String keyword) {
        if (status != null && !status.isEmpty()) {
            return borrowRecordRepository.findByStatusOrderByBorrowTimeDesc(status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            return borrowRecordRepository.search(null, keyword);
        }
        return borrowRecordRepository.findAllByOrderByBorrowTimeDesc();
    }

    /** 待审批列表（教师用） */
    public List<BorrowRecord> pendingApprovals() {
        List<BorrowRecord> list = borrowRecordRepository.findByStatusOrderByBorrowTimeDesc("PENDING");
        list.addAll(borrowRecordRepository.findByStatusOrderByBorrowTimeDesc("PENDING_EXTEND"));
        return list;
    }

    public Map<String, Object> stats() {
        Map<String, Object> stats = new HashMap<>();
        List<BorrowRecord> all = borrowRecordRepository.findAll();
        long borrowing = all.stream().filter(r -> "BORROWED".equals(r.getStatus())).count();
        long pending = all.stream().filter(r -> "PENDING".equals(r.getStatus()) || "PENDING_EXTEND".equals(r.getStatus())).count();
        long returnedToday = all.stream()
                .filter(r -> "RETURNED".equals(r.getStatus()) && r.getActualReturnTime() != null
                        && r.getActualReturnTime().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();
        stats.put("borrowingCount", borrowing);
        stats.put("pendingCount", pending);
        stats.put("returnedToday", returnedToday);
        stats.put("totalRecords", all.size());
        return stats;
    }

    // ==================== 日志 ====================
    private void log(String username, String action, String detail, String targetType, Long targetId) {
        try {
            OperationLog log = new OperationLog();
            log.setUsername(username);
            log.setRealName(username);
            log.setAction(action);
            log.setDetail(detail);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            operationLogRepository.save(log);
        } catch (Exception ignored) {}
    }
}

package com.example.borrow.service;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.entity.Material;
import com.example.borrow.entity.OperationLog;
import com.example.borrow.entity.User;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.repository.OperationLogRepository;
import com.example.borrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @Autowired private NotificationService notificationService;
    @Autowired private StockLogService stockLogService;
    @Autowired private CategoryService categoryService;
    @Autowired private SystemConfigService systemConfigService;
    @Autowired private UserRepository userRepository;
    @Autowired private UsageValidationService usageValidationService;
    @Autowired private CreditService creditService;

    // ==================== 借用申请（学生 → PENDING） ====================

    @Transactional
    public BorrowRecord requestBorrow(Long materialId, String borrower, String applicantUsername,
                                       Integer quantity, String purpose, LocalDateTime expectedReturnTime,
                                       String secondBorrower) {
        BorrowRecord record = buildPendingRecord(materialId, borrower, applicantUsername, quantity,
                purpose, expectedReturnTime, secondBorrower);
        log(borrower, "BORROW_REQUEST", "申请借用「" + record.getMaterialName() + "」×" + record.getBorrowQuantity()
                + ("MENTOR".equals(record.getApprovalStage()) ? "（先由导师审批）" : ""), "BORROW", record.getId());
        return record;
    }

    /** 一次实验所需的多项物资合并为一张申请单 */
    @Transactional
    public List<BorrowRecord> requestBatch(String borrower, String applicantUsername,
                                           String purpose, LocalDateTime expectedReturnTime,
                                           List<BatchLine> lines, boolean staff) {
        if (lines == null || lines.isEmpty()) {
            throw new RuntimeException("请至少选择一项物资");
        }
        String requestNo = "BR" + LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
        boolean directForStaff = staff;
        for (BatchLine line : lines) {
            Material material = materialRepository.findById(line.getMaterialId())
                    .orElseThrow(() -> new RuntimeException("物资不存在"));
            if (Boolean.TRUE.equals(material.getApprovalRequired())) {
                directForStaff = false;
            }
        }

        List<BorrowRecord> records = new ArrayList<>();
        for (BatchLine line : lines) {
            BorrowRecord record = buildPendingRecord(line.getMaterialId(), borrower, applicantUsername,
                    line.getQuantity(), purpose, expectedReturnTime, line.getSecondBorrower());
            record.setRequestNo(requestNo);
            record = borrowRecordRepository.save(record);
            records.add(record);
        }

        // 普通物资的教师/管理员批量出库仍免审批；只要有一项属于高值/需审批物资，
        // 整张申请统一进入审批，保证一次实验只需处理一次。
        if (directForStaff) {
            for (BorrowRecord record : records) {
                approveInternal(record, borrower, true);
            }
        } else {
            log(borrower, "BORROW_BATCH_REQUEST",
                    "提交批量借用申请 " + requestNo + "，共 " + records.size() + " 项物资",
                    "BORROW", records.get(0).getId());
        }
        return borrowRecordRepository.findByRequestNoOrderByBorrowTimeAsc(requestNo);
    }

    /**
     * 教师 / 超管自助借用：免审批。
     *
     * 与「学生申请 → 教师审批」两步流程的区别只有一点：不产生 PENDING 待办，
     * 建单后立刻扣库存并置为已借出（设备/玻璃器皿）或已领用（耗材/水/试剂）。
     * 数量上限、最长借用天数、库存与物资状态的校验口径，以及库存流水与操作日志，
     * 都与审批通道完全一致，因此自助借出的记录在「借用记录」里照样能归还、统计、追溯。
     */
    @Transactional
    public BorrowRecord borrowByStaff(Long materialId, String borrower, String applicantUsername,
                                      Integer quantity, String purpose, LocalDateTime expectedReturnTime,
                                      String secondBorrower) {
        BorrowRecord record = buildPendingRecord(materialId, borrower, applicantUsername, quantity,
                purpose, expectedReturnTime, secondBorrower);
        return approveInternal(record, borrower, true);
    }

    /**
     * 该物资是否允许教师 / 超管免审批自助出库。
     * 物资标记「借用需审批」时返回 false —— 高值、稀缺、收费或唯一一台的仪器，
     * 教师本人借用同样要进入审批流程，且审批人不能是自己（见 requireNotSelfApproval）。
     */
    public boolean canSelfBorrow(Long materialId) {
        return materialRepository.findById(materialId)
                .map(m -> !Boolean.TRUE.equals(m.getApprovalRequired()))
                .orElse(false);
    }

    /** 审批人不得是申请人本人。任何人（含超管）都不能用自己的账号审批自己的申请。 */
    private void requireNotSelfApproval(BorrowRecord record, String operator) {
        String currentUsername = com.example.borrow.security.UserContext.getUsername();
        String applicantUsername = record.getBorrowerUsername();
        if (currentUsername == null) return;
        if (applicantUsername != null) {
            if (!currentUsername.equals(applicantUsername)) return;
        } else if (!currentUsername.equals(record.getBorrower())
                && !record.getBorrower().equals(com.example.borrow.security.UserContext.getRealName())) {
            // 兼容升级前的历史记录；新记录必须走用户名比较
            return;
        }
        throw new RuntimeException("不能审批本人提交的申请；请让其他教师或管理员处理「"
                + record.getMaterialName() + "」这条申请");
    }

    /**
     * 申请人撤回借用申请（待审批阶段可撤回，撤回后申请作废，可重新提交）。
     * 学生误提交、或临时取消实验时不必再等教师拒绝。
     */
    @Transactional
    public void cancelRequest(Long recordId, String applicantUsername) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"PENDING".equals(record.getStatus()))
            throw new RuntimeException("只有待审批的申请才能撤回");
        User operator = applicantUsername != null
                ? userRepository.findByUsername(applicantUsername).orElse(null) : null;
        boolean isStaff = operator != null
                && ("TEACHER".equals(operator.getRole()) || "SUPER_ADMIN".equals(operator.getRole()));
        if (!isStaff && !isOwner(record, applicantUsername)) {
            throw new RuntimeException("只能撤回本人提交的申请");
        }
        record.setStatus("CANCELLED");
        record.setRejectReason("申请人主动撤回");
        borrowRecordRepository.save(record);
        log(record.getBorrower(), "BORROW_CANCEL", "撤回借用申请「" + record.getMaterialName()
                + "」×" + record.getBorrowQuantity(), "BORROW", recordId);
    }

    /** 校验 + 建单（状态 PENDING）：学生申请与教师自助借用共用同一套校验 */
    private BorrowRecord buildPendingRecord(Long materialId, String borrower, String applicantUsername,
                                            Integer quantity, String purpose, LocalDateTime expectedReturnTime,
                                            String secondBorrower) {
        if (quantity == null || quantity <= 0)
            throw new RuntimeException("借用数量必须大于0");

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("物资不存在"));
        requireAvailable(material);

        // 可归还型（设备/玻璃器皿）借出后需按期归还；消耗型（耗材/水/试剂）领用即核销、无归还义务。
        // 口径同 approveBorrow / 前端按钮判定，均以分类表 returnable 为准。
        boolean returnable = categoryService.isReturnable(material.getCategory());

        // ===== 系统配置校验（0=不开启）=====
        // 单次最多借用数量
        int maxItems = systemConfigService.getInt("borrow_max_items", 0);
        if (maxItems > 0 && quantity > maxItems) {
            throw new RuntimeException("单次最多借用 " + maxItems + " 件，本次申请 " + quantity + " 件");
        }
        // 单次借用最长天数（按自然日计，今天借、明天还 = 1 天）
        // 仅对可归还型生效：消耗型没有归还动作，强制填归还时间无意义，
        // 前端也不在这类物资上展示「预计归还」字段，否则领用会被永久拦下。
        int maxDays = systemConfigService.getInt("borrow_max_days", 0);
        if (maxDays > 0 && returnable) {
            if (expectedReturnTime == null) {
                throw new RuntimeException("请填写预计归还时间（最长可借 " + maxDays + " 天）");
            }
            long borrowDays = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(),
                    expectedReturnTime.toLocalDate());
            if (borrowDays <= 0) {
                throw new RuntimeException("预计归还时间必须晚于今天");
            }
            if (borrowDays > maxDays) {
                throw new RuntimeException("单次借用最长 " + maxDays + " 天，本次申请 " + borrowDays + " 天");
            }
        }
        // 消耗型：忽略误传的归还时间，避免落库出现"已领用却有归还日期"的误导数据
        if (!returnable) {
            expectedReturnTime = null;
        }

        if (material.getAvailableQuantity() < quantity)
            throw new RuntimeException("库存不足，当前可借数量：" + material.getAvailableQuantity());

        BorrowRecord record = new BorrowRecord();
        record.setMaterialId(materialId);
        record.setMaterialName(material.getName());
        record.setMaterialCategory(material.getCategory());
        record.setBorrower(borrower);
        record.setBorrowerUsername(applicantUsername);
        record.setBorrowQuantity(quantity);
        record.setStatus("PENDING");
        record.setBorrowTime(LocalDateTime.now());
        record.setExpectedReturnTime(expectedReturnTime);
        record.setPurpose(purpose);
        applyUsageRules(record, material, applicantUsername, secondBorrower);
        record = borrowRecordRepository.save(record);

        return record;
    }

    /**
     * 使用规则、费用与审批环节的统一落点（新建申请与「退回修改后重新提交」共用）。
     *
     * 1) 检定到期 / 培训资质 / 信用分三条准入校验；
     * 2) 危险化学品必须登记第二领用人（双人领用）；
     * 3) 预估费用：可归还型按「天 × 单价」，消耗型按「件 × 单价」；
     * 4) 审批环节：申请人配置了导师则先由导师审批，否则直接进入教师终审。
     */
    private void applyUsageRules(BorrowRecord record, Material material, String applicantUsername, String secondBorrower) {
        usageValidationService.validateUsage(material, applicantUsername, "借用或领用");

        if (Boolean.TRUE.equals(material.getHazardous())
                && (secondBorrower == null || secondBorrower.trim().isEmpty())) {
            throw new RuntimeException("「" + material.getName()
                    + "」属于危险化学品/管制类物资，需双人领用，请填写第二领用人姓名");
        }
        record.setSecondBorrower(secondBorrower);

        double unitPrice = material.getUnitPrice() != null ? material.getUnitPrice() : 0.0;
        double fee = 0.0;
        boolean returnable = categoryService.isReturnable(material.getCategory());
        if (unitPrice > 0) {
            if (returnable && record.getExpectedReturnTime() != null) {
                long days = Math.max(1, ChronoUnit.DAYS.between(
                        LocalDateTime.now().toLocalDate(), record.getExpectedReturnTime().toLocalDate()));
                fee = days * unitPrice;
            } else if (!returnable) {
                fee = record.getBorrowQuantity() * unitPrice;
            }
        }
        record.setEstimatedFee(Math.round(fee * 100) / 100.0);

        User applicant = applicantUsername != null
                ? userRepository.findByUsername(applicantUsername).orElse(null) : null;
        boolean hasMentor = applicant != null && applicant.getMentorName() != null
                && !applicant.getMentorName().trim().isEmpty();
        record.setApprovalStage(hasMentor ? "MENTOR" : "STAFF");
    }

    // ==================== 审批通过（教师） ====================

    @Transactional
    public BorrowRecord approveBorrow(Long recordId, String approver) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"PENDING".equals(record.getStatus()))
            throw new RuntimeException("该记录不是待审批状态");
        requireNotSelfApproval(record, approver);

        // 多级审批第一环节（导师/实验室负责人）：通过后只推进环节，不扣库存、不置为已借出
        if ("MENTOR".equals(record.getApprovalStage())) {
            record.setApprovalStage("STAFF");
            record.setMentorApprover(approver);
            record.setMentorApproveTime(LocalDateTime.now());
            record = borrowRecordRepository.save(record);

            notificationService.send(recipient(record), "借用申请已通过导师审批",
                    "您借用的「" + record.getMaterialName() + "」已由导师「" + approver
                            + "」通过，正在等待实验室管理员终审。", "SYSTEM");
            log(approver, "MENTOR_APPROVE", "导师审批通过「" + record.getBorrower() + "」借用「"
                    + record.getMaterialName() + "」", "BORROW", recordId);
            return record;
        }

        return approveInternal(record, approver, false);
    }

    /** 整张批量申请一次审批，任一明细库存不足则整单回滚 */
    @Transactional
    public List<BorrowRecord> approveBatch(String requestNo, String approver) {
        List<BorrowRecord> records = requireBatch(requestNo);
        for (BorrowRecord record : records) {
            if (!"PENDING".equals(record.getStatus())) {
                throw new RuntimeException("批量申请中存在非待审批记录，请刷新后重试");
            }
            requireNotSelfApproval(record, approver);
        }
        if ("MENTOR".equals(records.get(0).getApprovalStage())) {
            for (BorrowRecord record : records) {
                record.setApprovalStage("STAFF");
                record.setMentorApprover(approver);
                record.setMentorApproveTime(LocalDateTime.now());
                borrowRecordRepository.save(record);
            }
            notificationService.send(records.get(0).getBorrowerUsername(), "批量借用申请已通过导师审批",
                    "批量申请 " + requestNo + " 已通过导师审批，正在等待实验室管理员终审。", "SYSTEM");
        } else {
            for (BorrowRecord record : records) {
                approveInternal(record, approver, false);
            }
            notificationService.send(records.get(0).getBorrowerUsername(), "批量借用申请已通过",
                    "批量申请 " + requestNo + "（" + records.size() + " 项物资）已通过终审。", "SYSTEM");
        }
        return borrowRecordRepository.findByRequestNoOrderByBorrowTimeAsc(requestNo);
    }

    @Transactional
    public void rejectBatch(String requestNo, String approver, String reason) {
        List<BorrowRecord> records = requireBatch(requestNo);
        for (BorrowRecord record : records) {
            if (!"PENDING".equals(record.getStatus())) {
                throw new RuntimeException("批量申请中存在非待审批记录，请刷新后重试");
            }
            requireNotSelfApproval(record, approver);
            record.setStatus("REJECTED");
            record.setApprover(approver);
            record.setApproveTime(LocalDateTime.now());
            record.setRejectReason(reason);
            borrowRecordRepository.save(record);
        }
        notificationService.send(records.get(0).getBorrowerUsername(), "批量借用申请被拒绝",
                "批量申请 " + requestNo + " 已被拒绝。" + (reason != null ? "原因：" + reason : ""), "SYSTEM");
    }

    @Transactional
    public void returnBatchForEdit(String requestNo, String operator, String reason) {
        List<BorrowRecord> records = requireBatch(requestNo);
        for (BorrowRecord record : records) {
            if (!"PENDING".equals(record.getStatus())) {
                throw new RuntimeException("批量申请中存在非待审批记录，请刷新后重试");
            }
            requireNotSelfApproval(record, operator);
            record.setStatus("RETURNED_FOR_EDIT");
            record.setApprover(operator);
            record.setApproveTime(LocalDateTime.now());
            record.setRejectReason(reason);
            borrowRecordRepository.save(record);
        }
        notificationService.send(records.get(0).getBorrowerUsername(), "批量借用申请被退回修改",
                "批量申请 " + requestNo + " 已退回。" + (reason != null ? "原因：" + reason : ""), "SYSTEM");
    }

    @Transactional
    public void cancelBatch(String requestNo, String applicantUsername) {
        List<BorrowRecord> records = requireBatch(requestNo);
        for (BorrowRecord record : records) {
            if (!"PENDING".equals(record.getStatus())) {
                throw new RuntimeException("批量申请中存在非待审批记录，无法整单撤回");
            }
            if (!isOwner(record, applicantUsername)) {
                throw new RuntimeException("只能撤回本人提交的申请");
            }
            record.setStatus("CANCELLED");
            record.setRejectReason("申请人主动撤回");
            borrowRecordRepository.save(record);
        }
    }

    private List<BorrowRecord> requireBatch(String requestNo) {
        List<BorrowRecord> records = borrowRecordRepository.findByRequestNoOrderByBorrowTimeAsc(requestNo);
        if (records.isEmpty()) throw new RuntimeException("批量申请不存在或已处理");
        return records;
    }

    /** 批量申请中的单项明细 */
    public static class BatchLine {
        private Long materialId;
        private Integer quantity;
        private String secondBorrower;
        public Long getMaterialId() { return materialId; }
        public void setMaterialId(Long materialId) { this.materialId = materialId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getSecondBorrower() { return secondBorrower; }
        public void setSecondBorrower(String secondBorrower) { this.secondBorrower = secondBorrower; }
    }

    /**
     * 退回修改（开题报告约定的第三种审批操作：通过 / 拒绝 / 退回修改）。
     * 与「拒绝」的区别：拒绝是申请作废，退回是让申请人改完信息再走一遍审批。
     */
    @Transactional
    public void returnForEdit(Long recordId, String operator, String reason) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"PENDING".equals(record.getStatus()))
            throw new RuntimeException("只有待审批的申请才能退回修改");
        requireNotSelfApproval(record, operator);

        record.setStatus("RETURNED_FOR_EDIT");
        record.setApprover(operator);
        record.setApproveTime(LocalDateTime.now());
        record.setRejectReason(reason);
        borrowRecordRepository.save(record);

        notificationService.send(recipient(record), "借用申请被退回修改",
                "您借用的「" + record.getMaterialName() + "」申请被退回，请在「借用记录」中修改后重新提交。"
                        + (reason != null && !reason.isEmpty() ? "原因：" + reason : ""), "SYSTEM");
        log(operator, "RETURN_FOR_EDIT", "退回「" + record.getBorrower() + "」的借用申请（"
                + record.getMaterialName() + "）修改后重新提交", "BORROW", recordId);
    }

    /** 被退回的申请：申请人修改数量 / 归还时间 / 用途后重新提交，重新走审批流程 */
    @Transactional
    public BorrowRecord resubmit(Long recordId, String applicantUsername, Integer quantity,
                                 LocalDateTime expectedReturnTime, String purpose, String secondBorrower) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"RETURNED_FOR_EDIT".equals(record.getStatus()))
            throw new RuntimeException("只有被退回修改的申请才能重新提交");
        User operator = applicantUsername != null
                ? userRepository.findByUsername(applicantUsername).orElse(null) : null;
        boolean isStaff = operator != null
                && ("TEACHER".equals(operator.getRole()) || "SUPER_ADMIN".equals(operator.getRole()));
        if (!isStaff && !isOwner(record, applicantUsername)) {
            throw new RuntimeException("只能修改本人提交的申请");
        }
        if (quantity == null || quantity <= 0)
            throw new RuntimeException("借用数量必须大于0");

        Material material = materialRepository.findById(record.getMaterialId())
                .orElseThrow(() -> new RuntimeException("物资不存在"));
        requireAvailable(material);
        if (material.getAvailableQuantity() < quantity)
            throw new RuntimeException("库存不足，当前可借数量：" + material.getAvailableQuantity());

        int maxItems = systemConfigService.getInt("borrow_max_items", 0);
        if (maxItems > 0 && quantity > maxItems) {
            throw new RuntimeException("单次最多借用 " + maxItems + " 件，本次申请 " + quantity + " 件");
        }
        boolean returnable = categoryService.isReturnable(material.getCategory());
        int maxDays = systemConfigService.getInt("borrow_max_days", 0);
        if (maxDays > 0 && returnable) {
            if (expectedReturnTime == null) throw new RuntimeException("请填写预计归还时间（最长可借 " + maxDays + " 天）");
            long borrowDays = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), expectedReturnTime.toLocalDate());
            if (borrowDays <= 0) throw new RuntimeException("预计归还时间必须晚于今天");
            if (borrowDays > maxDays) throw new RuntimeException("单次借用最长 " + maxDays + " 天，本次申请 " + borrowDays + " 天");
        }
        if (!returnable) expectedReturnTime = null;

        record.setBorrowQuantity(quantity);
        record.setExpectedReturnTime(expectedReturnTime);
        record.setPurpose(purpose);
        record.setStatus("PENDING");
        record.setRejectReason(null);
        record.setApprover(null);
        record.setApproveTime(null);
        record.setBorrowTime(LocalDateTime.now());
        applyUsageRules(record, material, applicantUsername, secondBorrower);
        record = borrowRecordRepository.save(record);

        log(record.getBorrower(), "BORROW_RESUBMIT", "修改后重新提交借用申请「" + record.getMaterialName()
                + "」×" + quantity, "BORROW", recordId);
        return record;
    }

    /**
     * 出库 + 落状态 + 记库存流水 + 记操作日志（审批通过与教师自助借用共用）。
     *
     * @param selfService true = 教师/管理员自助借用（免审批）。只是文案与日志动作不同，
     *                    库存扣减与状态流转与审批通过完全一致
     */
    private BorrowRecord approveInternal(BorrowRecord record, String approver, boolean selfService) {
        Long recordId = record.getId();
        Material material = materialRepository.findByIdForUpdate(record.getMaterialId())
                .orElseThrow(() -> new RuntimeException("物资不存在"));
        requireAvailable(material);
        if (material.getAvailableQuantity() < record.getBorrowQuantity())
            throw new RuntimeException("库存不足，无法通过审批");

        material.setAvailableQuantity(material.getAvailableQuantity() - record.getBorrowQuantity());
        materialRepository.save(material);

        // 消耗型物资（耗材/水/试剂）：审批通过即核销库存，状态=已领用；可归还型（设备/玻璃器皿）：审批通过即借用中
        // 口径统一：以「分类表 t_category.returnable」为准（与前端预约/领用按钮的判定同源）；
        // 分类表里查不到该编码时才回退到内置规则，避免出现"前端当可归还、后端当消耗型"的两套标准
        boolean consumable = !categoryService.isReturnable(material.getCategory());
        record.setStatus(consumable ? "CONSUMED" : "BORROWED");
        record.setApprover(approver);
        record.setApproveTime(LocalDateTime.now());
        record = borrowRecordRepository.save(record);

        // 库存流水：消耗型记"领用核销"，可归还型记"借出"
        stockLogService.record(material, consumable ? "CONSUME" : "OUT_BORROW",
                -record.getBorrowQuantity(), "BORROW", recordId,
                (consumable ? (selfService ? "自助领用：" : "领用核销：")
                            : (selfService ? "自助借出：" : "审批借出：")) + record.getBorrower());

        if (selfService) {
            // 自助通道只记一条日志：写「申请+通过」两条会让人误以为走了审批
            log(approver, "BORROW_SELF",
                    (consumable ? "自助领用「" : "自助借用「") + record.getMaterialName() + "」×"
                            + record.getBorrowQuantity() + "（教师/管理员免审批）",
                    "BORROW", recordId);
        } else {
            log(approver, consumable ? "APPROVE_CONSUME" : "APPROVE",
                    (consumable ? "批准领用「" : "通过「") + record.getBorrower() + "」"
                            + (consumable ? "领用「" : "借用「") + record.getMaterialName() + "」",
                    "BORROW", recordId);
        }
        return record;
    }

    // ==================== 审批拒绝（教师） ====================

    @Transactional
    public void rejectBorrow(Long recordId, String approver, String reason) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        if (!"PENDING".equals(record.getStatus()))
            throw new RuntimeException("该记录不是待审批状态");
        requireNotSelfApproval(record, approver);

        record.setStatus("REJECTED");
        record.setApprover(approver);
        record.setApproveTime(LocalDateTime.now());
        record.setRejectReason(reason);
        borrowRecordRepository.save(record);

        notificationService.send(recipient(record),
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
        requireNotSelfApproval(record, approver);

        int days = record.getExtensionDays() != null ? record.getExtensionDays() : 7;
        LocalDateTime newReturnTime = (record.getExpectedReturnTime() != null
                ? record.getExpectedReturnTime() : LocalDateTime.now()).plusDays(days);
        record.setExpectedReturnTime(newReturnTime);
        record.setStatus("BORROWED");
        record.setExtensionApproved(true);
        record.setExtensionDays(null);
        borrowRecordRepository.save(record);

        notificationService.send(recipient(record),
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
        requireNotSelfApproval(record, approver);

        // 恢复原状态
        LocalDateTime now = LocalDateTime.now();
        if (record.getExpectedReturnTime() != null && record.getExpectedReturnTime().isBefore(now))
            record.setStatus("OVERDUE");
        else
            record.setStatus("BORROWED");
        record.setExtensionDays(null);
        borrowRecordRepository.save(record);

        notificationService.send(recipient(record),
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

        // ===== 数量校验（缺陷2修复）：0 < 归还数量 ≤ 未归还数量 =====
        int total = record.getBorrowQuantity() != null ? record.getBorrowQuantity() : 0;
        int alreadyReturned = record.getReturnedQuantity() != null ? record.getReturnedQuantity() : 0;
        int remain = total - alreadyReturned;
        if (remain <= 0) {
            throw new RuntimeException("该记录已全部归还，无需再次归还");
        }
        boolean damaged = isDamaged != null && isDamaged;

        // 归还数量：不传默认归还剩余全部；损坏归还按剩余部分一次性交回
        int actualReturn = returnQuantity != null ? returnQuantity : remain;
        if (actualReturn <= 0) {
            throw new RuntimeException("归还数量必须大于 0");
        }
        if (actualReturn > remain) {
            throw new RuntimeException("归还数量（" + actualReturn + "）不能超过未归还数量（" + remain + "）");
        }
        if (damaged) {
            actualReturn = remain;
        }

        // 归还物资：只有实际交回的数量回到可借库存
        Material material = materialRepository.findByIdForUpdate(record.getMaterialId())
                .orElseThrow(() -> new RuntimeException("物资不存在"));
        material.setAvailableQuantity(material.getAvailableQuantity() + actualReturn);
        materialRepository.save(material);

        // 库存流水：归还入库
        stockLogService.record(material, "RETURN", actualReturn, "BORROW", recordId,
                "归还：" + record.getBorrower() + "（" + actualReturn + "/" + remain + " 件）");

        // ===== 部分归还（缺陷1修复）：未还完不结单，剩余数量继续挂在同一条记录上 =====
        int newReturned = alreadyReturned + actualReturn;
        int remainAfter = total - newReturned;
        boolean finished = damaged || newReturned >= total;

        int overdueDays = 0;
        double fineAmount = 0;
        if (finished) {
            // 逾期天数与罚款在整单结清时计算
            if (record.getExpectedReturnTime() != null) {
                LocalDateTime deadline = record.getExpectedReturnTime();
                LocalDateTime now = LocalDateTime.now();
                if (now.isAfter(deadline)) {
                    overdueDays = (int) ChronoUnit.DAYS.between(deadline.toLocalDate(), now.toLocalDate());
                    if (overdueDays > 0) {
                        int finePerDay = systemConfigService.getInt("overdue_fine_per_day", 0);
                        fineAmount = overdueDays * finePerDay;
                    }
                }
            }
            record.setOverdueDays(overdueDays);
            record.setFineAmount(fineAmount);
            record.setActualReturnTime(LocalDateTime.now());

            if (damaged) {
                record.setStatus("DAMAGED");
                record.setIsDamaged(true);
                record.setDamageDescription(damageDescription);
                // 设备损坏处理：物资转「维修中」，修复前不再允许借用；
                // 修好后由管理员在「物资管理」中把状态改回「可用」
                material.setDeviceStatus("MAINTENANCE");
                materialRepository.save(material);
                notificationService.send(recipient(record), "物资损坏登记",
                    "您归还的「" + record.getMaterialName() + "」已登记为损坏，物品转维修处理。"
                        + (damageDescription != null && !damageDescription.isEmpty() ? "损坏说明：" + damageDescription : ""),
                    "DAMAGE");
            } else {
                record.setStatus("RETURNED");
            }
        }

        record.setReturnedQuantity(newReturned);

        // 归还备注：部分归还时追加剩余提示，便于在列表上直接看清还欠多少
        String remark = returnRemark;
        if (!finished) {
            String note = "部分归还 " + actualReturn + " 件，剩余 " + remainAfter + " 件未归还";
            remark = (returnRemark != null && !returnRemark.isEmpty()) ? returnRemark + "；" + note : note;
        }
        record.setReturnRemark(remark);

        record = borrowRecordRepository.save(record);

        String detail = "归还「" + record.getMaterialName() + "」" + actualReturn + " 件"
                + (damaged ? "（有损坏，已转维修）" : (finished ? "" : "（部分归还，剩余 " + remainAfter + " 件）"));
        if (fineAmount > 0) detail += "，逾期" + overdueDays + "天，罚款¥" + fineAmount;
        log(record.getBorrower(), damaged ? "DAMAGE" : "RETURN", detail, "BORROW", recordId);

        // 信用分：逾期按天扣分（每天 5 分、单次最多 20 分），按时完好归还 +1 分（上限 100）
        if (finished) {
            if (overdueDays > 0) {
                creditService.changeScore(recipient(record), -Math.min(20, overdueDays * 5),
                        "借用「" + record.getMaterialName() + "」逾期 " + overdueDays + " 天归还");
            } else if (!damaged) {
                creditService.changeScore(recipient(record), 1,
                        "按时归还「" + record.getMaterialName() + "」");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("overdueDays", overdueDays);
        result.put("fineAmount", fineAmount);
        result.put("returnedQuantity", newReturned);
        result.put("remainQuantity", remainAfter);
        result.put("finished", finished);
        return result;
    }

    /**
     * 预约时段结束后自动结单，仅用于预约审批生成的借用记录。
     * 手动提前归还会先把记录置为已归还，因此本方法可重复调用而不会重复加库存。
     */
    @Transactional
    public boolean autoCloseReservationBorrow(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId).orElse(null);
        if (record == null || !Arrays.asList("BORROWED", "OVERDUE", "PENDING_EXTEND")
                .contains(record.getStatus())) {
            return false;
        }
        int total = record.getBorrowQuantity() != null ? record.getBorrowQuantity() : 0;
        int returned = record.getReturnedQuantity() != null ? record.getReturnedQuantity() : 0;
        int remain = Math.max(0, total - returned);
        if (remain > 0) {
            Material material = materialRepository.findByIdForUpdate(record.getMaterialId())
                    .orElseThrow(() -> new RuntimeException("物资不存在"));
            material.setAvailableQuantity(
                    (material.getAvailableQuantity() != null ? material.getAvailableQuantity() : 0) + remain);
            materialRepository.save(material);
            stockLogService.record(material, "RETURN", remain, "BORROW", recordId,
                    "预约时段结束自动归还：" + record.getBorrower());
        }
        record.setReturnedQuantity(total);
        record.setActualReturnTime(LocalDateTime.now());
        record.setStatus("RETURNED");
        record.setExtensionDays(null);
        record.setReturnRemark("预约时段结束，系统自动结单，无需手动归还");
        borrowRecordRepository.save(record);
        notificationService.send(recipient(record), "预约已自动结单",
                "您预约使用的「" + record.getMaterialName() + "」时段已结束，系统已自动完成结单。",
                "SYSTEM");
        log(recipient(record), "RESERVATION_AUTO_RETURN",
                "预约时段结束自动归还「" + record.getMaterialName() + "」×" + remain,
                "BORROW", recordId);
        return true;
    }

    // ==================== 列表 / 统计 ====================

    /**
     * 列表查询：状态 / 关键字（物资名·借用人）/ 借用人（精确，"只看我的"）三者可同时生效。
     * 缺陷7修复：此前前端是用"本人姓名覆盖关键字"来做权限过滤的，导致按物资名搜索失效。
     */
    public Page<BorrowRecord> list(String status, String keyword, String borrowerName,
                                   String borrowerUsername, Pageable pageable) {
        String s = (status != null && !status.isEmpty()) ? status : null;
        String k = (keyword != null && !keyword.isEmpty()) ? keyword : null;
        String name = (borrowerName != null && !borrowerName.isEmpty()) ? borrowerName : null;
        String username = (borrowerUsername != null && !borrowerUsername.isEmpty())
                ? borrowerUsername : null;
        return borrowRecordRepository.searchAdvanced(s, k, name, username, pageable);
    }

    /** 按 ID 取记录（供控制器做归属校验用） */
    public BorrowRecord getById(Long recordId) {
        return borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
    }

    /** 待审批列表（教师用） */
    public List<BorrowRecord> pendingApprovals() {
        List<BorrowRecord> list = borrowRecordRepository.findByStatusOrderByBorrowTimeDesc("PENDING");
        list.addAll(borrowRecordRepository.findByStatusOrderByBorrowTimeDesc("PENDING_EXTEND"));
        return list;
    }

    /**
     * 借用记录统计。
     *
     * borrower 为空 = 全量口径（教师/超管）；不为空 = 只统计该借用人（学生）。
     * 之前的实现是纯全量，学生端页面顶部四张卡片显示的是全校数字，
     * 而下方表格只有自己的记录，同一屏里两个口径打架。
     * 姓名精确匹配，与 list() 的 borrower 过滤同一口径。
     */
    public Map<String, Object> stats(String borrowerUsername) {
        Map<String, Object> stats = new HashMap<>();
        List<BorrowRecord> all = borrowRecordRepository.findAll();
        if (borrowerUsername != null && !borrowerUsername.isEmpty()) {
            all = all.stream()
                    .filter(r -> borrowerUsername.equals(r.getBorrowerUsername())
                            || r.getBorrowerUsername() == null
                            && borrowerUsername.equals(r.getBorrower()))
                    .collect(java.util.stream.Collectors.toList());
        }
        // 缺陷6修复：逾期同样是"未归还"，之前只数 BORROWED 会把逾期记录漏掉
        long borrowing = all.stream()
                .filter(r -> "BORROWED".equals(r.getStatus()) || "OVERDUE".equals(r.getStatus())).count();
        long overdue = all.stream().filter(r -> "OVERDUE".equals(r.getStatus())).count();
        long pending = all.stream().filter(r -> "PENDING".equals(r.getStatus()) || "PENDING_EXTEND".equals(r.getStatus())).count();
        long returnedToday = all.stream()
                .filter(r -> "RETURNED".equals(r.getStatus()) && r.getActualReturnTime() != null
                        && r.getActualReturnTime().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();
        stats.put("borrowingCount", borrowing);
        stats.put("overdueCount", overdue);
        stats.put("pendingCount", pending);
        stats.put("returnedToday", returnedToday);
        stats.put("totalRecords", all.size());
        return stats;
    }

    /** 校验物资状态：维修中 / 已报废的物资不可借用或领用 */
    private void requireAvailable(Material material) {
        String s = material.getDeviceStatus();
        if (s != null && !"AVAILABLE".equals(s)) {
            String text = "MAINTENANCE".equals(s) ? "维修中" : ("SCRAPPED".equals(s) ? "已报废" : s);
            throw new RuntimeException("该物资当前为「" + text + "」，不可借用或领用");
        }
    }

    /** 归属判断以稳定用户名为准；历史记录没有用户名时才回退到姓名。 */
    private boolean isOwner(BorrowRecord record, String username) {
        if (record == null || username == null) return false;
        return username.equals(record.getBorrowerUsername())
                || record.getBorrowerUsername() == null
                && (username.equals(record.getBorrower())
                    || record.getBorrower().equals(com.example.borrow.security.UserContext.getRealName()));
    }

    /** 通知优先发给稳定用户名，避免同名用户收到错误消息。 */
    private String recipient(BorrowRecord record) {
        return record.getBorrowerUsername() != null
                ? record.getBorrowerUsername() : record.getBorrower();
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

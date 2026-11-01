package com.example.borrow.service;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.entity.Material;
import com.example.borrow.entity.OperationLog;
import com.example.borrow.entity.Reservation;
import com.example.borrow.entity.User;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.repository.OperationLogRepository;
import com.example.borrow.repository.ReservationRepository;
import com.example.borrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 预约服务（方案B：预约→审批→借用→归还 闭环）
 *
 * 流程：
 *   1. 学生提交预约  → 状态 PENDING（待审批），占用该时间段（冲突检测含 PENDING）
 *   2. 教师/超管审批  → 通过：状态 APPROVED，并自动生成一条借用记录
 *      （可归还型= BORROWED 借用中，可直接在「借用记录」里归还；消耗型= CONSUMED 已领用并核销库存）
 *      → 拒绝：状态 REJECTED，通知预约人
 *   3. 审批通过后，后续归还/逾期/损坏全部复用已有的借用记录流程
 *   4. 取消：仅 PENDING（待审批）可取消；APPROVED 已生成借用记录，需走归还
 */
@Service
public class ReservationService {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private BorrowRecordRepository borrowRecordRepository;
    @Autowired private OperationLogRepository operationLogRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private StockLogService stockLogService;
    @Autowired private CategoryService categoryService;
    @Autowired private SystemConfigService systemConfigService;
    @Autowired private UsageValidationService usageValidationService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ==================== 创建预约（→ PENDING 待审批） ====================

    @Transactional
    public Reservation createReservation(Long materialId, String username, String realName,
                                          LocalDateTime startTime, LocalDateTime endTime,
                                          String purpose, Integer attendeeCount,
                                          boolean staffSelfService) {
        return doCreate(materialId, username, realName, startTime, endTime,
                purpose, attendeeCount, true, staffSelfService);
    }

    /**
     * 创建预约的实际实现（事务由调用方保证）。
     *
     * @param enforceAdvanceLimit 是否校验「最多提前 N 天」。
     *        单次预约需要校验；周期预约展开的是未来数周（第 4 周已在 21 天之后），
     *        与提前天数上限天然冲突，故周期预约豁免该项，但仍受「最短时长」约束。
     *        若周期预约的跨度需要收紧，应限制 weeks 参数，而不是用提前天数去拦。
     */
    private Reservation doCreate(Long materialId, String username, String realName,
                                  LocalDateTime startTime, LocalDateTime endTime,
                                  String purpose, Integer attendeeCount,
                                  boolean enforceAdvanceLimit, boolean staffSelfService) {
        // 1. 校验物资是否存在
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("物资不存在"));

        // 2. 物资状态校验（维修中 / 已报废不可预约）
        requireAvailable(material);

        // 2.1 使用前统一校验：检定/维护到期、培训资质、信用分
        usageValidationService.validateUsage(material, username, "预约");

        // 3. 时间段合法性
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new RuntimeException("开始时间必须早于结束时间");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("不能预约过去的时间");
        }

        // 3.1 预约最短时长（系统配置 reserve_min_hours，0=不开启）
        double minHours = systemConfigService.getDouble("reserve_min_hours", 0);
        if (minHours > 0) {
            double hours = ChronoUnit.MINUTES.between(startTime, endTime) / 60.0;
            if (hours < minHours - 1e-9) {
                throw new RuntimeException("预约时长不得少于 " + trim(minHours) + " 小时，当前为 "
                        + trim(hours) + " 小时");
            }
        }

        // 3.2 预约最多提前天数（系统配置 reserve_advance_days，0=不开启）
        if (enforceAdvanceLimit) {
            int advanceDays = systemConfigService.getInt("reserve_advance_days", 0);
            if (advanceDays > 0) {
                long aheadDays = ChronoUnit.DAYS.between(LocalDate.now(), startTime.toLocalDate());
                if (aheadDays > advanceDays) {
                    throw new RuntimeException("最多只能提前 " + advanceDays + " 天预约，本次提前 "
                            + aheadDays + " 天");
                }
            }
        }

        // 4. 冲突检测（待审批的预约同样占位）
        List<Reservation> conflicts = reservationRepository.findConflicting(materialId, startTime, endTime);
        if (!conflicts.isEmpty()) {
            Reservation c = conflicts.get(0);
            throw new RuntimeException("时段冲突：该物资在 "
                    + c.getStartTime().format(FMT) + " ~ " + c.getEndTime().format(FMT)
                    + " 已被「" + c.getRealName() + "」预约"
                    + ("PENDING".equals(c.getStatus()) ? "（待审批）" : ""));
        }

        // 库存硬约束：已借出数量已经反映在 availableQuantity 中。
        // 没有可用实物时不再只做提示，而是直接拒绝预约，避免“预约成功却无设备可用”。
        if (material.getAvailableQuantity() == null || material.getAvailableQuantity() <= 0) {
            throw new RuntimeException("「" + material.getName()
                    + "」当前没有可用实物（可能已全部借出），请等待归还后再预约");
        }

        // 5. 创建预约：进入待审批，由教师/管理员审批后生效
        Reservation r = new Reservation();
        r.setMaterialId(materialId);
        r.setMaterialName(material.getName());
        r.setMaterialCategory(material.getCategory());
        r.setUsername(username);
        r.setRealName(realName);
        r.setStartTime(startTime);
        r.setEndTime(endTime);
        r.setType("SINGLE");
        r.setStatus("PENDING");
        r.setPurpose(purpose);
        r.setAttendeeCount(attendeeCount);
        // 预估机时费用：预约时长（小时）× 机时单价（0 表示不收费）
        double hourlyPrice = material.getHourlyPrice() != null ? material.getHourlyPrice() : 0.0;
        if (hourlyPrice > 0) {
            double hours = ChronoUnit.MINUTES.between(startTime, endTime) / 60.0;
            r.setEstimatedFee(Math.round(hours * hourlyPrice * 100) / 100.0);
        } else {
            r.setEstimatedFee(0.0);
        }
        Reservation saved = reservationRepository.save(r);

        // 6. 普通物资的教师/管理员预约登记即生效；高值/稀缺物资仍进入审批。
        boolean direct = staffSelfService && !Boolean.TRUE.equals(material.getApprovalRequired());
        if (direct) {
            notificationService.send(saved.getUsername(), "预约已登记",
                    "您预约的「" + material.getName() + "」（" + startTime.format(FMT) + " ~ "
                            + endTime.format(FMT) + "）已登记生效，无需审批。", "SYSTEM");
        } else {
            notificationService.send(saved.getUsername(), "预约申请已提交",
                    "您预约的「" + material.getName() + "」（" + startTime.format(FMT) + " ~ "
                            + endTime.format(FMT) + "）已提交，等待教师审批。", "SYSTEM");
            notifyTeachers("有新的预约待审批",
                    saved.getRealName() + " 申请预约「" + material.getName() + "」（" + startTime.format(FMT)
                            + " ~ " + endTime.format(FMT) + "），请及时审批。");
        }

        log(saved.getUsername(), "RESERVE",
                "申请预约「" + material.getName() + "」" + startTime.format(FMT) + " ~ " + endTime.format(FMT),
                "RESERVATION", saved.getId());
        if (direct) {
            approveInternal(saved, realName, true);
        }
        return saved;
    }

    /** 创建周期性预约（自动展开未来 N 周，每周一条待审批预约） */
    @Transactional
    public List<Reservation> createRecurring(Long materialId, String username, String realName,
                                              LocalDateTime firstStart, LocalDateTime firstEnd,
                                              String recurringRule, int weeks,
                                              String purpose, boolean staffSelfService) {
        List<Reservation> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int w = 0; w < weeks; w++) {
            LocalDateTime s = firstStart.plusWeeks(w * getWeekMultiplier(recurringRule));
            LocalDateTime e = firstEnd.plusWeeks(w * getWeekMultiplier(recurringRule));
            try {
                created.add(doCreate(materialId, username, realName, s, e,
                        purpose + "（第" + (w + 1) + "次）", 1, false, staffSelfService));
            } catch (RuntimeException ex) {
                errors.add(ex.getMessage());
            }
        }

        if (created.isEmpty() && !errors.isEmpty()) {
            throw new RuntimeException("周期预约全部失败：" + String.join("；", errors));
        }

        // 更新类型标记
        for (Reservation r : created) {
            r.setType("RECURRING");
            r.setRecurringRule(recurringRule);
            reservationRepository.save(r);
        }

        return created;
    }

    private int getWeekMultiplier(String rule) {
        if ("BIWEEKLY".equals(rule)) return 2;
        if ("MONTHLY".equals(rule)) return 4;
        return 1; // WEEKLY or default
    }

    // ==================== 审批（教师/管理员） ====================

    /**
     * 审批通过：
     *  - 预约状态置 APPROVED
     *  - 扣减库存 + 记库存流水
     *  - 自动生成借用记录（可归还型=BORROWED 借用中；消耗型=CONSUMED 已领用）
     *  → 生成的借用记录可直接在「借用记录」中归还 / 登记损坏，形成业务闭环
     */
    @Transactional
    public Map<String, Object> approve(Long reservationId, String approver) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("预约不存在，ID: " + reservationId));
        return approveInternal(r, approver, false);
    }

    /** 审批通过的核心逻辑；自助登记（selfService=true）跳过自审校验但保留完整留痕。 */
    private Map<String, Object> approveInternal(Reservation r, String approver, boolean selfService) {
        if (!"PENDING".equals(r.getStatus())) {
            throw new RuntimeException("该预约当前状态为「" + statusLabel(r.getStatus()) + "」，不是待审批状态");
        }
        if (!selfService) requireNotSelfApproval(r, approver);

        Material material = materialRepository.findByIdForUpdate(r.getMaterialId())
                .orElseThrow(() -> new RuntimeException("物资不存在"));
        requireAvailable(material);

        int quantity = 1; // 预约按 1 件/台计
        if (material.getAvailableQuantity() == null || material.getAvailableQuantity() < quantity) {
            throw new RuntimeException("库存不足，当前可借数量："
                    + (material.getAvailableQuantity() == null ? 0 : material.getAvailableQuantity()));
        }

        boolean returnable = categoryService.isReturnable(material.getCategory());

        // 1. 扣减库存
        material.setAvailableQuantity(material.getAvailableQuantity() - quantity);
        materialRepository.save(material);

        // 2. 生成借用/领用记录
        BorrowRecord record = new BorrowRecord();
        record.setMaterialId(material.getId());
        record.setMaterialName(material.getName());
        record.setMaterialCategory(material.getCategory());
        record.setBorrower(r.getRealName() != null ? r.getRealName() : r.getUsername());
        record.setBorrowerUsername(r.getUsername());
        record.setBorrowQuantity(quantity);
        record.setStatus(returnable ? "BORROWED" : "CONSUMED");
        record.setBorrowTime(r.getStartTime() != null ? r.getStartTime() : LocalDateTime.now());
        record.setExpectedReturnTime(r.getEndTime());
        record.setPurpose((r.getPurpose() != null && !r.getPurpose().isEmpty() ? r.getPurpose() : "预约借用")
                + "（预约单#" + r.getId() + "）");
        record.setApprover(approver);
        record.setApproveTime(LocalDateTime.now());
        record = borrowRecordRepository.save(record);

        // 3. 库存流水
        stockLogService.record(material, returnable ? "OUT_BORROW" : "CONSUME", -quantity, "BORROW", record.getId(),
                (returnable ? "预约审批借出：" : "预约审批领用核销：") + record.getBorrower());

        // 4. 更新预约
        r.setStatus("APPROVED");
        r.setApprover(approver);
        r.setApproveTime(LocalDateTime.now());
        r.setBorrowRecordId(record.getId());
        reservationRepository.save(r);

        // 5. 通知预约人
        notificationService.send(r.getUsername(), "预约申请已通过",
                "您预约的「" + material.getName() + "」（" + r.getStartTime().format(FMT) + " ~ "
                        + r.getEndTime().format(FMT) + "）已批准"
                        + (returnable ? "，已生成借用单，请使用后按时归还。" : "，请按时领用。"), "SYSTEM");

        log(approver, selfService ? "RESERVE_SELF" : (returnable ? "APPROVE" : "APPROVE_CONSUME"),
                (selfService ? "自助登记「" : "通过「") + r.getRealName() + "」的预约「" + material.getName() + "」"
                        + r.getStartTime().format(FMT) + " ~ " + r.getEndTime().format(FMT)
                        + (returnable ? "，已生成借用单 #" + record.getId() : "，已核销库存"),
                "RESERVATION", r.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reservation", r);
        result.put("borrowRecord", record);
        result.put("statusLabel", statusLabel(r.getStatus()));
        return result;
    }

    /** 审批拒绝：预约置 REJECTED，不产生任何库存/借用变动 */
    @Transactional
    public void reject(Long reservationId, String approver, String reason) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("预约不存在，ID: " + reservationId));
        requireNotSelfApproval(r, approver);
        if (!"PENDING".equals(r.getStatus())) {
            throw new RuntimeException("该预约当前状态为「" + statusLabel(r.getStatus()) + "」，不是待审批状态");
        }

        r.setStatus("REJECTED");
        r.setApprover(approver);
        r.setApproveTime(LocalDateTime.now());
        r.setRejectReason(reason);
        reservationRepository.save(r);

        notificationService.send(r.getUsername(), "预约申请被拒绝",
                "您预约的「" + r.getMaterialName() + "」（" + r.getStartTime().format(FMT) + " ~ "
                        + r.getEndTime().format(FMT) + "）申请已被拒绝。"
                        + (reason != null && !reason.isEmpty() ? "原因：" + reason : ""), "SYSTEM");

        log(approver, "REJECT", "拒绝「" + r.getRealName() + "」的预约「" + r.getMaterialName() + "」",
                "RESERVATION", r.getId());
    }

    // ==================== 查询 ====================

    /** 按 ID 取预约（供控制器做归属校验用） */
    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
    }

    /** 待审批预约（教师审批页签 / 角标） */
    public List<Reservation> pendingList() {
        return reservationRepository.findByStatusInOrderByCreateTimeDesc(Arrays.asList("PENDING"));
    }

    /** 实时冲突检测（供前端实时校验） */
    public Map<String, Object> checkConflict(Long materialId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        List<Reservation> conflicts = reservationRepository.findConflicting(materialId, startTime, endTime);
        result.put("hasConflict", !conflicts.isEmpty());
        if (!conflicts.isEmpty()) {
            List<Map<String, Object>> conflictList = new ArrayList<>();
            for (Reservation c : conflicts) {
                Map<String, Object> item = new HashMap<>();
                item.put("startTime", c.getStartTime().toString());
                item.put("endTime", c.getEndTime().toString());
                item.put("reservedBy", c.getRealName());
                item.put("purpose", c.getPurpose());
                item.put("status", c.getStatus());
                conflictList.add(item);
            }
            result.put("conflicts", conflictList);
        }

        // 在借提示：走「直接借用」通道借走的设备不占用预约时段，
        // 上面的时段冲突检测查不到它，但设备实物已不在库，预约后可能取不到 —— 单独返回给前端提示。
        // 这里只提示不阻断：同款设备可能还有余量，是否放行由教师在审批环节按库存把关。
        List<BorrowRecord> borrowedOut = borrowRecordRepository.findByMaterialIdAndStatusIn(
                materialId, java.util.Arrays.asList("BORROWED", "OVERDUE", "PENDING_EXTEND"));
        result.put("borrowedOutCount", borrowedOut.size());
        if (!borrowedOut.isEmpty()) {
            List<Map<String, Object>> borrowInfo = new ArrayList<>();
            LocalDateTime earliest = null;
            int borrowedQty = 0;
            for (BorrowRecord b : borrowedOut) {
                Map<String, Object> item = new HashMap<>();
                item.put("borrower", b.getBorrower());
                item.put("quantity", b.getBorrowQuantity());
                item.put("status", b.getStatus());
                item.put("expectedReturnTime",
                        b.getExpectedReturnTime() != null ? b.getExpectedReturnTime().toString() : null);
                borrowInfo.add(item);
                borrowedQty += b.getBorrowQuantity() == null ? 0 : b.getBorrowQuantity();
                if (b.getExpectedReturnTime() != null
                        && (earliest == null || b.getExpectedReturnTime().isBefore(earliest))) {
                    earliest = b.getExpectedReturnTime();
                }
            }
            result.put("borrowedOut", borrowInfo);
            result.put("borrowedOutQuantity", borrowedQty);
            result.put("earliestReturnTime", earliest != null ? earliest.toString() : null);
        }
        return result;
    }

    /** 获取某物资某周的预约（用于日历展示） */
    public List<Reservation> getWeekReservations(Long materialId, LocalDateTime weekStart, LocalDateTime weekEnd) {
        return reservationRepository.findByMaterialAndDateRange(materialId, weekStart, weekEnd);
    }

    /** 获取所有物资某周的预约（用于日历总览） */
    public List<Map<String, Object>> getWeekOverview(LocalDateTime weekStart, LocalDateTime weekEnd) {
        List<Material> allMaterials = materialRepository.findAll();
        List<Map<String, Object>> overview = new ArrayList<>();

        for (Material m : allMaterials) {
            if (!"AVAILABLE".equals(m.getDeviceStatus())) continue;

            List<Reservation> reservations = reservationRepository
                    .findByMaterialAndDateRange(m.getId(), weekStart, weekEnd);
            if (!reservations.isEmpty()) {
                Map<String, Object> item = new HashMap<>();
                item.put("materialId", m.getId());
                item.put("materialName", m.getName());
                item.put("category", m.getCategory());
                item.put("reservations", reservations);
                overview.add(item);
            }
        }
        return overview;
    }

    /** 取消预约：仅待审批状态可取消；已通过的预约已生成借用记录，需走归还 */
    @Transactional
    public void cancel(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        if ("CANCELLED".equals(r.getStatus())) {
            throw new RuntimeException("该预约已取消");
        }
        if (!"PENDING".equals(r.getStatus())) {
            throw new RuntimeException("已通过的预约已生成借用记录，不能直接取消，请在「借用记录」中归还或联系管理员");
        }
        r.setStatus("CANCELLED");
        reservationRepository.save(r);

        notificationService.send(r.getUsername(), "预约已取消",
                "「" + r.getMaterialName() + "」（" + r.getStartTime().format(FMT) + " ~ "
                        + r.getEndTime().format(FMT) + "）的预约已取消。", "SYSTEM");
        log(r.getUsername(), "CANCEL", "取消预约「" + r.getMaterialName() + "」", "RESERVATION", r.getId());
    }

    /** 用户预约列表 */
    public List<Reservation> listByUser(String username) {
        return reservationRepository.findByUsernameOrderByCreateTimeDesc(username);
    }

    /** 全部预约列表（管理员查看） */
    public List<Reservation> listAll(String status) {
        if (status != null && !status.isEmpty())
            return reservationRepository.findByStatusOrderByCreateTimeDesc(status);
        return reservationRepository.findAll();
    }

    // ==================== 工具方法 ====================

    /**
     * 审批人不得是预约人本人（超管作为系统责任人保留处理权限）。
     * 与借用审批同一口径：审批审的是配额与责任，自己批自己等于审批环节归零。
     */
    private void requireNotSelfApproval(Reservation r, String approver) {
        String currentUsername = com.example.borrow.security.UserContext.getUsername();
        if (currentUsername == null) return;
        if (!currentUsername.equals(r.getUsername())) return;
        throw new RuntimeException("不能审批本人提交的预约；请让其他教师或管理员处理「"
                + r.getMaterialName() + "」这条预约");
    }

    /** 物资状态校验：维修中 / 已报废的物资不可预约 */
    private void requireAvailable(Material material) {
        String s = material.getDeviceStatus();
        if (s != null && !"AVAILABLE".equals(s)) {
            String text = "MAINTENANCE".equals(s) ? "维修中" : ("SCRAPPED".equals(s) ? "已报废" : s);
            throw new RuntimeException("该物资当前为「" + text + "」，不可预约或审批通过");
        }
    }

    /** 数值展示：整数不带小数点，让提示语读起来自然（2 而不是 2.0） */
    private String trim(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9) return String.valueOf((long) Math.rint(v));
        return String.valueOf(Math.round(v * 100) / 100.0);
    }

    private String statusLabel(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待审批";
            case "APPROVED": return "已通过";
            case "COMPLETED": return "已完成";
            case "REJECTED": return "已拒绝";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }

    /** 给全部教师推送通知（预约待审批提醒） */
    private void notifyTeachers(String title, String content) {
        List<User> teachers = userRepository.findByRole("TEACHER");
        for (User t : teachers) {
            notificationService.send(t.getUsername(), title, content, "SYSTEM");
        }
    }

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

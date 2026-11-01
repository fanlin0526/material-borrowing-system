package com.example.borrow.task;

import com.example.borrow.entity.OperationLog;
import com.example.borrow.entity.Reservation;
import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.OperationLogRepository;
import com.example.borrow.repository.ReservationRepository;
import com.example.borrow.service.NotificationService;
import com.example.borrow.service.BorrowService;
import com.example.borrow.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 预约超时自动作废任务（让配置项 auto_cancel_hours 真正生效）
 *
 * 起算口径：从「预约时段开始时刻」起算，而不是「提交预约时刻」。
 *   判定条件：startTime + auto_cancel_hours &lt; now 且状态仍为 PENDING。
 *
 * 选这个口径的理由是责任归属：若从提交时刻起算，教师没能及时审批的后果
 * 会被转嫁给学生（学生正常提交、教师周末没看，周一预约就全被系统作废）；
 * 而按时段开始起算，作废的是「时段已经开始、却始终无人处理」的占位，
 * 该时段同时被释放给他人使用，这正是自动作废应有的价值。
 *
 * 配置 auto_cancel_hours &lt;= 0 时任务直接返回（0=不开启）。
 */
@Component
public class ReservationAutoCancelTask {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private OperationLogRepository operationLogRepository;
    @Autowired private SystemConfigService systemConfigService;
    @Autowired private NotificationService notificationService;
    @Autowired private BorrowService borrowService;
    @Autowired private BorrowRecordRepository borrowRecordRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 每小时检查一次（与逾期检测同频） */
    @Scheduled(fixedRate = 3600000, initialDelay = 60000)
    public void autoCancelTimeoutReservations() {
        int hours = systemConfigService.getInt("auto_cancel_hours", 0);
        LocalDateTime now = LocalDateTime.now();
        int cancelled = 0;
        if (hours > 0) {
            List<Reservation> pendings =
                    reservationRepository.findByStatusInOrderByCreateTimeDesc(Arrays.asList("PENDING"));
            for (Reservation r : pendings) {
                if (r.getStartTime() == null) continue;
                // 起算点 = 预约时段开始 + 配置小时数；还没到就继续等教师审批
                if (r.getStartTime().plusHours(hours).isAfter(now)) continue;

                String reason = "预约时段开始后超过 " + hours + " 小时仍未处理，系统已自动作废并释放该时段";
                r.setStatus("CANCELLED");
                // 复用 rejectReason 承载作废原因，让用户在预约列表里能看到「为什么被取消」
                r.setRejectReason(reason);
                reservationRepository.save(r);
                cancelled++;

                notificationService.send(r.getUsername(), "预约已自动作废",
                        "您预约的「" + r.getMaterialName() + "」（" + r.getStartTime().format(FMT) + " ~ "
                                + r.getEndTime().format(FMT) + "）" + reason + "。如仍需使用，请重新提交预约。",
                        "SYSTEM");

                log(r, reason);
            }
        }

        if (cancelled > 0) {
            System.out.println(">>> 预约超时自动作废：" + cancelled + " 条");
        }

        // 已通过预约在时段结束后自动结单：
        // 1）回补预约审批时扣减的库存；2）生成归还流水；3）记录实际预约机时。
        int completed = 0;
        for (Reservation r : reservationRepository.findByStatusOrderByCreateTimeDesc("APPROVED")) {
            if (r.getEndTime() == null || r.getEndTime().isAfter(now) || r.getBorrowRecordId() == null) {
                continue;
            }
            BorrowRecord record = borrowRecordRepository.findById(r.getBorrowRecordId()).orElse(null);
            if (record == null) continue;
            // 手工提前归还后，借用单已经结清；这里只更新预约状态，不重复回补库存。
            if (Arrays.asList("BORROWED", "OVERDUE", "PENDING_EXTEND").contains(record.getStatus())) {
                borrowService.autoCloseReservationBorrow(record.getId());
            }
            if (r.getStartTime() != null) {
                r.setUsedHours(Math.round(
                        (java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes() / 60.0)
                                * 10) / 10.0);
            }
            r.setStatus("COMPLETED");
            reservationRepository.save(r);
            completed++;
        }
        if (completed > 0) {
            System.out.println(">>> 预约时段结束自动结单：" + completed + " 条");
        }
    }

    private void log(Reservation r, String reason) {
        try {
            OperationLog log = new OperationLog();
            log.setUsername("system");
            log.setRealName("系统");
            log.setAction("AUTO_CANCEL");
            log.setDetail("自动作废「" + r.getRealName() + "」的预约「" + r.getMaterialName() + "」：" + reason);
            log.setTargetType("RESERVATION");
            log.setTargetId(r.getId());
            operationLogRepository.save(log);
        } catch (Exception ignored) {}
    }
}

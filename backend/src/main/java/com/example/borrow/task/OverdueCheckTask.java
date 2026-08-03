package com.example.borrow.task;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OverdueCheckTask {

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private NotificationService notificationService;

    /** 每小时检查一次逾期 */
    @Scheduled(fixedRate = 3600000)
    public void checkOverdue() {
        List<BorrowRecord> records = borrowRecordRepository.findByStatusOrderByBorrowTimeDesc("BORROWED");
        int overdueCount = 0;
        for (BorrowRecord r : records) {
            // 有预计归还时间且已过期，且尚未标记为逾期
            if (r.getExpectedReturnTime() != null
                    && r.getExpectedReturnTime().isBefore(LocalDateTime.now())) {
                if (!"OVERDUE".equals(r.getStatus())) {
                    r.setStatus("OVERDUE");
                    borrowRecordRepository.save(r);
                    overdueCount++;
                    // 发送逾期通知
                    notificationService.send(r.getBorrower(),
                            "物资逾期提醒",
                            "您借用的「" + r.getMaterialName() + "」已于 "
                                    + r.getExpectedReturnTime().toLocalDate() + " 到期，请尽快归还。",
                            "OVERDUE");
                }
            }
        }
        if (overdueCount > 0) {
            System.out.println(">>> 逾期检测：标记 " + overdueCount + " 条逾期记录");
        }
    }
}

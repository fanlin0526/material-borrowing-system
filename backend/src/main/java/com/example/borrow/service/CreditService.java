package com.example.borrow.service;

import com.example.borrow.entity.OperationLog;
import com.example.borrow.entity.User;
import com.example.borrow.repository.OperationLogRepository;
import com.example.borrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 信用分服务。
 *
 * 借用记录里存的是姓名（borrower），因此按姓名定位账号；
 * 分数区间固定 0~100，逾期归还按天扣分、按时归还少量加分，
 * 低于系统配置 credit_min_score 时由 {@link UsageValidationService} 拦下新的借用与预约。
 */
@Service
public class CreditService {

    @Autowired private UserRepository userRepository;
    @Autowired private OperationLogRepository operationLogRepository;

    /** 优先按用户名调整信用分；历史调用传入姓名时再按姓名兼容查找 */
    public Integer changeScore(String usernameOrRealName, int delta, String reason) {
        if (usernameOrRealName == null || usernameOrRealName.isEmpty()) return null;
        User user = userRepository.findByUsername(usernameOrRealName).orElse(null);
        if (user == null) {
            List<User> users = userRepository.findByRealName(usernameOrRealName);
            if (users.isEmpty()) return null;
            user = users.get(0);
        }
        int before = user.getCreditScore() != null ? user.getCreditScore() : 100;
        int after = Math.max(0, Math.min(100, before + delta));
        if (after == before) return after;

        user.setCreditScore(after);
        userRepository.save(user);
        try {
            OperationLog log = new OperationLog();
            log.setUsername(user.getUsername());
            log.setRealName(user.getRealName());
            log.setAction("CREDIT_CHANGE");
            log.setDetail("信用分 " + before + " → " + after + "（" + (delta > 0 ? "+" : "") + delta
                    + "）：" + reason);
            log.setTargetType("USER");
            log.setTargetId(user.getId());
            operationLogRepository.save(log);
        } catch (Exception ignored) {}
        return after;
    }
}

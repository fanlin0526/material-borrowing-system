package com.example.borrow.service;

import com.example.borrow.entity.Material;
import com.example.borrow.entity.User;
import com.example.borrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 使用前统一校验（借用 / 领用 / 预约共用一套口径）。
 *
 * 汇总三条真实实验室普遍存在的准入规则，避免各业务类各写一份：
 *  1. 计量检定 / 维护到期：到期后暂停使用，先检定再放开；
 *  2. 培训资质：needTraining 的仪器必须持有有效授权记录；
 *  3. 信用分：低于系统阈值时暂停借用与预约（逾期、爽约扣分）。
 */
@Service
public class UsageValidationService {

    @Autowired private TrainingService trainingService;
    @Autowired private SystemConfigService systemConfigService;
    @Autowired private UserRepository userRepository;

    public void validateUsage(Material material, String username, String action) {
        // 1. 检定 / 维护到期
        if (material.getNextMaintenanceDate() != null
                && material.getNextMaintenanceDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("「" + material.getName() + "」的检定/维护已于 "
                    + material.getNextMaintenanceDate() + " 到期，按实验室计量管理要求暂停" + action
                    + "；请先完成检定并在物资管理中更新「下次检定日期」");
        }

        // 2. 培训资质
        if (Boolean.TRUE.equals(material.getNeedsTraining())
                && !trainingService.hasValidTraining(material.getId(), username)) {
            throw new RuntimeException("「" + material.getName() + "」需要培训授权后才能" + action
                    + "，当前账号没有有效的培训记录，请联系教师登记资质");
        }

        // 3. 信用分
        int minScore = systemConfigService.getInt("credit_min_score", 60);
        if (minScore > 0 && username != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                int score = user.getCreditScore() != null ? user.getCreditScore() : 100;
                if (score < minScore) {
                    throw new RuntimeException("当前信用分 " + score + " 分，低于系统要求的最低 "
                            + minScore + " 分，暂时无法" + action + "；请先归还逾期物资或联系管理员");
                }
            }
        }
    }
}

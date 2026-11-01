package com.example.borrow.controller;

import com.example.borrow.entity.Material;
import com.example.borrow.entity.OperationLog;
import com.example.borrow.entity.TrainingRecord;
import com.example.borrow.entity.User;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.repository.OperationLogRepository;
import com.example.borrow.repository.TrainingRecordRepository;
import com.example.borrow.repository.UserRepository;
import com.example.borrow.security.RoleGuard;
import com.example.borrow.security.UserContext;
import com.example.borrow.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 培训 / 上机资质授权接口。
 * 维护（新增、删除）仅教师与超管；查询所有登录用户可用（借用人要能看自己有什么资质）。
 */
@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired private TrainingRecordRepository trainingRecordRepository;
    @Autowired private TrainingService trainingService;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OperationLogRepository operationLogRepository;

    /** 按物资查已授权名单 */
    @GetMapping
    public Result<?> listByMaterial(@RequestParam Long materialId) {
        return Result.ok(trainingService.listByMaterial(materialId));
    }

    /** 全部资质记录（教师 / 超管用于总览） */
    @GetMapping("/all")
    public Result<?> listAll() {
        return Result.ok(trainingService.listAll());
    }

    /** 我的资质 */
    @GetMapping("/mine")
    public Result<?> mine() {
        return Result.ok(trainingService.listByUsername(UserContext.getUsername()));
    }

    /**
     * 可授权的用户候选名单（学生）。
     * 教师没有「用户管理」权限，这里单独提供一个只读的轻量名单用于登记资质。
     */
    @GetMapping("/candidates")
    public Result<?> candidates() {
        RoleGuard.requireStaff();
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        for (User u : userRepository.findByRole("STUDENT")) {
            java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("username", u.getUsername());
            row.put("realName", u.getRealName());
            row.put("department", u.getDepartment());
            row.put("labName", u.getLabName());
            list.add(row);
        }
        return Result.ok(list);
    }

    /** 登记培训授权（教师 / 超管） */
    @PostMapping
    public Result<?> add(@RequestBody Map<String, String> body) {
        RoleGuard.requireStaff();
        Long materialId = parseLong(body.get("materialId"));
        String username = body.get("username");
        if (materialId == null) return Result.fail("请选择物资");
        if (username == null || username.trim().isEmpty()) return Result.fail("请选择被授权人");

        Material material = materialRepository.findById(materialId).orElse(null);
        if (material == null) return Result.fail("物资不存在");
        User user = userRepository.findByUsername(username.trim()).orElse(null);
        if (user == null) return Result.fail("用户不存在");

        // 同一人同一物资只保留一条最新资质，重复登记视为延期/更新
        TrainingRecord record = trainingRecordRepository
                .findByMaterialIdAndUsername(materialId, username.trim())
                .stream().findFirst().orElse(new TrainingRecord());
        record.setMaterialId(materialId);
        record.setMaterialName(material.getName());
        record.setUsername(user.getUsername());
        record.setRealName(user.getRealName());
        record.setTrainer(body.getOrDefault("trainer", RoleGuard.currentOperator()));
        record.setTrainDate(parseDate(body.get("trainDate"), LocalDate.now()));
        record.setValidUntil(parseDate(body.get("validUntil"), null));
        record.setRemark(body.get("remark"));
        trainingRecordRepository.save(record);

        log("TRAINING_GRANT", "登记培训资质：「" + user.getRealName() + "」可操作「" + material.getName() + "」"
                + (record.getValidUntil() != null ? "，有效期至 " + record.getValidUntil() : "，长期有效"),
                material.getId());
        return Result.ok(record);
    }

    /** 撤销资质 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        RoleGuard.requireStaff();
        TrainingRecord record = trainingRecordRepository.findById(id).orElse(null);
        if (record == null) return Result.fail("记录不存在");
        trainingRecordRepository.delete(record);
        log("TRAINING_REVOKE", "撤销培训资质：「" + record.getRealName() + "」的「"
                + record.getMaterialName() + "」", record.getMaterialId());
        return Result.ok();
    }

    private Long parseLong(String s) {
        try { return s == null ? null : Long.parseLong(s.trim()); } catch (Exception e) { return null; }
    }

    private LocalDate parseDate(String s, LocalDate def) {
        if (s == null || s.trim().isEmpty()) return def;
        try {
            // 兼容前端 date-picker 直接传 yyyy-MM-dd 与带时间的 ISO 串
            return LocalDate.parse(s.length() > 10 ? s.substring(0, 10) : s.trim());
        } catch (Exception e) { return def; }
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

package com.example.borrow.controller;

import com.example.borrow.entity.OperationLog;
import com.example.borrow.entity.SystemConfig;
import com.example.borrow.entity.User;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.OperationLogRepository;
import com.example.borrow.repository.SystemConfigRepository;
import com.example.borrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private BorrowRecordRepository borrowRecordRepository;
    @Autowired private SystemConfigRepository systemConfigRepository;
    @Autowired private OperationLogRepository operationLogRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ==================== 用户管理 ====================

    /** 用户列表 */
    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(defaultValue = "") String keyword) {
        List<User> users;
        if (keyword.isEmpty()) {
            users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        } else {
            users = userRepository.findByRealNameContainingOrUsernameContaining(keyword, keyword);
        }
        users.forEach(u -> u.setPassword(null)); // 脱敏
        return Result.ok(users);
    }

    /** 新增用户 */
    @PostMapping("/users")
    public Result<?> addUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.getOrDefault("password", "123456");
        String realName = body.getOrDefault("realName", username);
        String role = body.getOrDefault("role", "STUDENT");
        String phone = body.get("phone");

        if (username == null || username.trim().isEmpty()) return Result.fail("用户名不能为空");
        if (userRepository.existsByUsername(username.trim())) return Result.fail("用户名已存在");

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName.trim());
        user.setRole(role);
        user.setPhone(phone);
        user.setDepartment(body.get("department"));
        user.setLabName(body.get("labName"));
        user.setMentorName(body.get("mentorName"));
        user.setCreditScore(100);
        user.setEnabled(true);
        // 管理员代开的账号用的是默认密码，首次登录强制改密
        user.setPasswordChanged(false);
        userRepository.save(user);
        return Result.ok("创建成功");
    }

    /** 编辑用户 */
    @PutMapping("/users/{id}")
    public Result<?> editUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.fail("用户不存在");

        if (body.containsKey("realName")) user.setRealName(body.get("realName"));
        if (body.containsKey("role")) user.setRole(body.get("role"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        if (body.containsKey("department")) user.setDepartment(body.get("department"));
        if (body.containsKey("labName")) user.setLabName(body.get("labName"));
        if (body.containsKey("mentorName")) user.setMentorName(body.get("mentorName"));
        if (body.containsKey("creditScore")) {
            try { user.setCreditScore(Integer.parseInt(body.get("creditScore"))); } catch (Exception ignored) {}
        }
        userRepository.save(user);
        return Result.ok("更新成功");
    }

    /**
     * 启用 / 停用账号（论文「调整用户角色、重置密码、停用或启用账号」）。
     * 停用只是禁止登录与提交新业务，历史记录与操作日志全部保留。
     */
    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.fail("用户不存在");
        Object enabled = body.get("enabled");
        if (enabled == null) return Result.fail("缺少参数 enabled");
        boolean target = Boolean.parseBoolean(String.valueOf(enabled));
        if (!target && "SUPER_ADMIN".equals(user.getRole())) {
            return Result.fail("超级管理员账号不允许停用");
        }
        user.setEnabled(target);
        userRepository.save(user);

        try {
            OperationLog log = new OperationLog();
            log.setUsername(com.example.borrow.security.UserContext.getUsername());
            log.setRealName(com.example.borrow.security.UserContext.getRealName());
            log.setAction("USER_STATUS");
            log.setDetail("账号「" + user.getRealName() + "（" + user.getUsername() + "）」"
                    + (target ? "已启用" : "已停用"));
            log.setTargetType("USER");
            log.setTargetId(user.getId());
            operationLogRepository.save(log);
        } catch (Exception ignored) {}

        return Result.ok(target ? "已启用" : "已停用");
    }

    /** 重置密码 */
    @PutMapping("/users/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.fail("用户不存在");
        user.setPassword(passwordEncoder.encode("123456"));
        // 重置为默认密码后，要求本人首次登录强制修改
        user.setPasswordChanged(false);
        userRepository.save(user);
        return Result.ok("密码已重置为 123456");
    }

    /**
     * 删除用户。
     * 论文口径：存在未归还借用记录的账号不允许删除，提示改为「停用」——
     * 因为记录里存的是姓名，物理删人会留下查不到人的历史记录。
     */
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.fail("用户不存在");
        java.util.List<com.example.borrow.entity.BorrowRecord> mine =
                borrowRecordRepository.findByBorrowerUsernameOrderByBorrowTimeDesc(user.getUsername());
        if (mine.isEmpty() && user.getRealName() != null) {
            // 兼容升级前只保存姓名的历史记录
            mine = borrowRecordRepository.findByBorrowerContainingOrderByBorrowTimeDesc(user.getRealName());
        }
        long unfinished = mine.stream().filter(r -> java.util.Arrays.asList(
                "PENDING", "BORROWED", "OVERDUE", "PENDING_EXTEND", "RETURNED_FOR_EDIT")
                .contains(r.getStatus())).count();
        if (unfinished > 0) {
            return Result.fail("该用户还有 " + unfinished + " 条未完结的借用/延期记录，不能删除；"
                    + "建议改用「停用账号」，历史记录可保留追溯。");
        }
        userRepository.deleteById(id);
        return Result.ok("删除成功");
    }

    // ==================== 系统配置 ====================

    /** 获取所有配置 */
    @GetMapping("/config")
    public Result<?> listConfig() {
        List<SystemConfig> configs = systemConfigRepository.findAll();
        Map<String, String> map = new HashMap<>();
        for (SystemConfig c : configs) {
            map.put(c.getConfigKey(), c.getConfigValue());
        }
        return Result.ok(map);
    }

    /** 保存配置 */
    @PostMapping("/config")
    public Result<?> saveConfig(@RequestBody List<Map<String, String>> configs) {
        for (Map<String, String> item : configs) {
            String key = item.get("configKey");
            String value = item.get("configValue");
            String desc = item.getOrDefault("description", "");
            SystemConfig config = systemConfigRepository.findByConfigKey(key).orElse(new SystemConfig());
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(desc);
            systemConfigRepository.save(config);
        }
        return Result.ok("配置已保存");
    }

    // ==================== 操作日志 ====================

    /** 日志列表（最近 200 条） */
    @GetMapping("/logs")
    public Result<?> listLogs() {
        List<OperationLog> logs = operationLogRepository.findAll(
            PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "createTime"))
        ).getContent();
        return Result.ok(logs);
    }
}

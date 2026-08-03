package com.example.borrow.controller;

import com.example.borrow.entity.OperationLog;
import com.example.borrow.entity.SystemConfig;
import com.example.borrow.entity.User;
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
        userRepository.save(user);
        return Result.ok("更新成功");
    }

    /** 重置密码 */
    @PutMapping("/users/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.fail("用户不存在");
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
        return Result.ok("密码已重置为 123456");
    }

    /** 删除用户 */
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return Result.fail("用户不存在");
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

package com.example.borrow.controller;

import com.example.borrow.entity.User;
import com.example.borrow.repository.UserRepository;
import com.example.borrow.security.JwtUtil;
import com.example.borrow.security.UserContext;
import com.example.borrow.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SystemConfigService systemConfigService;

    /** 登录 */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }
        User user = userRepository.findByUsername(username.trim()).orElse(null);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        // 停用账号禁止登录（历史数据 enabled 为空视为启用）
        if (Boolean.FALSE.equals(user.getEnabled())) {
            return Result.fail("该账号已停用，请联系管理员");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.fail("密码错误");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        data.put("department", user.getDepartment());
        data.put("labName", user.getLabName());
        data.put("mentorName", user.getMentorName());
        data.put("creditScore", user.getCreditScore() != null ? user.getCreditScore() : 100);
        // 管理员重置过密码 → 本次登录必须改密
        data.put("needChangePassword", Boolean.FALSE.equals(user.getPasswordChanged()));
        data.put("token", JwtUtil.generate(user.getUsername(), user.getRole(), user.getRealName()));

        return Result.ok(data);
    }

    /** 注册 */
    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String realName = body.get("realName");

        if (username == null || username.trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }
        if (realName == null || realName.trim().isEmpty()) {
            return Result.fail("真实姓名不能为空");
        }
        if (body.get("department") == null || body.get("department").trim().isEmpty()) {
            return Result.fail("所属学院或部门不能为空");
        }
        // 自助注册开关：默认开启（与论文「用户在登录页可自行注册账号」一致），
        // 关闭后账号只能由管理员在「用户管理」中开通
        if (!systemConfigService.getBoolean("allow_self_register", true)) {
            return Result.fail("系统已关闭自助注册，请通过管理员开通账号");
        }
        if (userRepository.existsByUsername(username.trim())) {
            return Result.fail("用户名已存在");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName.trim());
        user.setRole("STUDENT");
        user.setPhone(body.get("phone"));
        user.setEnabled(true);
        // 自助注册的密码是本人设置的，无需强制改密
        user.setPasswordChanged(true);
        user.setCreditScore(100);
        user.setDepartment(body.get("department"));
        user.setLabName(body.get("labName"));
        userRepository.save(user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("token", JwtUtil.generate(user.getUsername(), user.getRole(), user.getRealName()));

        return Result.ok(data);
    }

    /** 获取当前用户信息 */
    @GetMapping("/info")
    public Result<?> info() {
        String username = UserContext.getUsername();
        if (username == null) return Result.fail("未登录");
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Result.fail("用户不存在");
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("phone", user.getPhone());
        data.put("avatar", user.getAvatar());
        data.put("department", user.getDepartment());
        data.put("labName", user.getLabName());
        data.put("mentorName", user.getMentorName());
        data.put("creditScore", user.getCreditScore() != null ? user.getCreditScore() : 100);
        data.put("needChangePassword", Boolean.FALSE.equals(user.getPasswordChanged()));
        return Result.ok(data);
    }

    /** 修改个人资料 */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody Map<String, String> body) {
        String username = UserContext.getUsername();
        if (username == null) return Result.fail("未登录");
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Result.fail("用户不存在");

        if (body.containsKey("realName")) {
            user.setRealName(body.get("realName").trim());
        }
        if (body.containsKey("phone")) {
            user.setPhone(body.get("phone").trim());
        }
        userRepository.save(user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("phone", user.getPhone());
        return Result.ok(data);
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> body) {
        String username = UserContext.getUsername();
        if (username == null) return Result.fail("未登录");
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return Result.fail("用户不存在");

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty()) return Result.fail("原密码不能为空");
        if (newPassword == null || newPassword.isEmpty()) return Result.fail("新密码不能为空");
        if (newPassword.length() < 6) return Result.fail("新密码至少6位");

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.fail("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(true);
        userRepository.save(user);
        return Result.ok();
    }
}

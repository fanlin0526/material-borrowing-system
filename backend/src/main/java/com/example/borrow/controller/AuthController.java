package com.example.borrow.controller;

import com.example.borrow.entity.User;
import com.example.borrow.repository.UserRepository;
import com.example.borrow.security.JwtUtil;
import com.example.borrow.security.UserContext;
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
        data.put("token", JwtUtil.generate(user.getUsername(), user.getRole()));

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
        if (userRepository.existsByUsername(username.trim())) {
            return Result.fail("用户名已存在");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName != null ? realName.trim() : username.trim());
        user.setRole("STUDENT");
        user.setPhone(body.get("phone"));
        userRepository.save(user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("token", JwtUtil.generate(user.getUsername(), user.getRole()));

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
        userRepository.save(user);
        return Result.ok();
    }
}

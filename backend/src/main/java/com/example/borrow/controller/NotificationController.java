package com.example.borrow.controller;

import com.example.borrow.service.NotificationService;
import com.example.borrow.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /** 通知列表 */
    @GetMapping
    public Result<?> list() {
        return Result.ok(notificationService.list(currentUsername()));
    }

    /** 未读数量 */
    @GetMapping("/unread")
    public Result<?> unreadCount() {
        return Result.ok(notificationService.unreadCount(currentUsername()));
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<?> readAll() {
        notificationService.markAllRead(currentUsername());
        return Result.ok();
    }

    /** 单条标记已读 */
    @PutMapping("/read/{id}")
    public Result<?> readOne(@PathVariable Long id) {
        notificationService.markRead(id, currentUsername());
        return Result.ok();
    }

    private String currentUsername() {
        String username = UserContext.getUsername();
        if (username == null || username.isEmpty()) {
            throw new RuntimeException("未登录");
        }
        return username;
    }
}

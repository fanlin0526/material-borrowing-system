package com.example.borrow.controller;

import com.example.borrow.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /** 通知列表 */
    @GetMapping
    public Result<?> list(@RequestParam String username) {
        return Result.ok(notificationService.list(username));
    }

    /** 未读数量 */
    @GetMapping("/unread")
    public Result<?> unreadCount(@RequestParam String username) {
        return Result.ok(notificationService.unreadCount(username));
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<?> readAll(@RequestParam String username) {
        notificationService.markAllRead(username);
        return Result.ok();
    }

    /** 单条标记已读 */
    @PutMapping("/read/{id}")
    public Result<?> readOne(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.ok();
    }
}

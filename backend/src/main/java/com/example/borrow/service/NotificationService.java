package com.example.borrow.service;

import com.example.borrow.entity.Notification;
import com.example.borrow.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /** 发送通知 */
    public Notification send(String username, String title, String content, String type) {
        Notification n = new Notification();
        n.setUsername(username);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setIsRead(false);
        return notificationRepository.save(n);
    }

    /** 获取用户通知列表 */
    public List<Notification> list(String username) {
        return notificationRepository.findByUsernameOrderByCreateTimeDesc(username);
    }

    /** 获取未读数量 */
    public Map<String, Object> unreadCount(String username) {
        long count = notificationRepository.countByUsernameAndIsReadFalse(username);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return result;
    }

    /** 全部标记已读 */
    public void markAllRead(String username) {
        List<Notification> list = notificationRepository.findByUsernameAndIsReadFalseOrderByCreateTimeDesc(username);
        for (Notification n : list) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(list);
    }

    /** 标记单条已读 */
    public void markRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }
}

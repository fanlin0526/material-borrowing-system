package com.example.borrow.service;

import com.example.borrow.entity.Notification;
import com.example.borrow.entity.User;
import com.example.borrow.repository.NotificationRepository;
import com.example.borrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 发送通知
     *
     * @param username 接收人用户名；调用方传「真实姓名」时会自动解析成用户名
     *                 （借用/预约流程里拿到的只有借用人姓名，而通知查询一律按用户名，必须统一）
     */
    public Notification send(String username, String title, String content, String type) {
        Notification n = new Notification();
        n.setUsername(resolveUsername(username));
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setIsRead(false);
        return notificationRepository.save(n);
    }

    /** 把「真实姓名」解析成用户名；传入的已是用户名则原样返回 */
    private String resolveUsername(String nameOrUsername) {
        if (nameOrUsername == null || nameOrUsername.isEmpty()) return nameOrUsername;
        if (userRepository.findByUsername(nameOrUsername).isPresent()) return nameOrUsername;
        List<User> sameName = userRepository.findByRealName(nameOrUsername);
        return sameName.isEmpty() ? nameOrUsername : sameName.get(0).getUsername();
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

    /** 标记单条已读：只能操作当前登录人的通知，不能凭通知 ID 越权修改他人消息 */
    public void markRead(Long id, String username) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        if (username == null || !username.equals(n.getUsername())) {
            throw new RuntimeException("权限不足：只能操作自己的通知");
        }
        n.setIsRead(true);
        notificationRepository.save(n);
    }
}

package com.example.borrow.repository;

import com.example.borrow.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUsernameOrderByCreateTimeDesc(String username);
    List<Notification> findByUsernameAndIsReadFalseOrderByCreateTimeDesc(String username);
    long countByUsernameAndIsReadFalse(String username);
}

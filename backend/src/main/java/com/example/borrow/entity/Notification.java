package com.example.borrow.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 接收人用户名 */
    @Column(length = 50, nullable = false)
    private String username;

    /** 通知标题 */
    @Column(length = 200, nullable = false)
    private String title;

    /** 通知内容 */
    @Column(length = 500)
    private String content;

    /** 类型：OVERDUE-逾期提醒，DAMAGE-损坏通知，SYSTEM-系统通知 */
    @Column(length = 20, nullable = false)
    private String type;

    /** 是否已读 */
    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

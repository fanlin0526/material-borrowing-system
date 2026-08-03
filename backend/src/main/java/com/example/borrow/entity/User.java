package com.example.borrow.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名 */
    @Column(unique = true, length = 50, nullable = false)
    private String username;

    /** 密码（BCrypt哈希） */
    @Column(length = 200, nullable = false)
    private String password;

    /** 真实姓名 */
    @Column(length = 50)
    private String realName;

    /** 角色：ADMIN-管理员，USER-普通用户 */
    @Column(length = 20, nullable = false)
    private String role;

    /** 手机号 */
    @Column(length = 20)
    private String phone;

    /** 头像URL */
    @Column(length = 255)
    private String avatar;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

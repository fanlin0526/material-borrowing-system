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

    /**
     * 账号是否启用（null 视为启用，兼容加字段前的历史数据）。
     * 停用后不能登录、不能提交借用/预约，但历史记录与操作日志保留 —— 与论文「停用或启用账号」一致。
     */
    private Boolean enabled = true;

    /**
     * 密码是否由本人修改过。
     * null 视为「无需强制改密」（兼容历史账号，避免演示账号一登录就被强制改密）；
     * 管理员重置密码后会显式置为 false，该账号下次登录必须改密。
     */
    private Boolean passwordChanged = true;

    /** 所属学院 / 部门（用于按院系统计与考核） */
    @Column(length = 50)
    private String department;

    /** 所属实验室 / 课题组 */
    @Column(length = 50)
    private String labName;

    /** 导师（实验室负责人）姓名：借用申请的第一审批环节由其审批，为空则直接进入教师终审 */
    @Column(length = 50)
    private String mentorName;

    /** 信用分：逾期归还、爽约占用会扣分，低于阈值时禁止提交借用与预约 */
    private Integer creditScore = 100;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

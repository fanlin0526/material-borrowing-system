package com.example.borrow.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_operation_log")
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 操作人用户名 */
    @Column(length = 50, nullable = false)
    private String username;

    /** 操作人姓名 */
    @Column(length = 50)
    private String realName;

    /** 操作类型：LOGIN/LOGOUT/BORROW/RETURN/RESERVE/CANCEL/MATERIAL_ADD/MATERIAL_EDIT/MATERIAL_DELETE/USER_EDIT/CONFIG_EDIT */
    @Column(length = 30, nullable = false)
    private String action;

    /** 操作描述 */
    @Column(length = 500)
    private String detail;

    /** 操作目标类型 */
    @Column(length = 50)
    private String targetType;

    /** 操作目标ID */
    private Long targetId;

    /** 客户端IP */
    @Column(length = 50)
    private String ip;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

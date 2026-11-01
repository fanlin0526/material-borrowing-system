package com.example.borrow.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物资ID */
    @Column(nullable = false)
    private Long materialId;

    /** 物资名称(冗余) */
    @Column(length = 100)
    private String materialName;

    /** 物资类别(冗余) */
    @Column(length = 20)
    private String materialCategory;

    /** 预约人用户名 */
    @Column(length = 50, nullable = false)
    private String username;

    /** 预约人真实姓名 */
    @Column(length = 50)
    private String realName;

    /** 开始时间 */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /** 结束时间 */
    @Column(nullable = false)
    private LocalDateTime endTime;

    /** 类型：SINGLE-单次，RECURRING-周期 */
    @Column(length = 20, nullable = false)
    private String type;

    /** 周期规则：WEEKLY/BIWEEKLY/MONTHLY（type=RECURRING时有效） */
    @Column(length = 20)
    private String recurringRule;

    /**
     * 状态：PENDING-待审批（新建即此状态，需教师/管理员审批），
     * APPROVED-已通过（审批通过后自动生成借用/领用记录），
     * REJECTED-已拒绝，CANCELLED-已取消，COMPLETED-时段结束并自动结单
     */
    @Column(length = 20, nullable = false)
    private String status;

    /** 用途说明 */
    @Column(length = 500)
    private String purpose;

    /** 预约人数 */
    private Integer attendeeCount;

    /** 审批人 */
    @Column(length = 50)
    private String approver;

    /** 审批时间 */
    private LocalDateTime approveTime;

    /** 拒绝原因 */
    @Column(length = 500)
    private String rejectReason;

    /** 审批通过后自动生成的借用/领用记录ID（形成 预约→审批→借用→归还 闭环） */
    private Long borrowRecordId;

    /** 预估机时费用（预约时长 × 物资机时单价；0 表示该物资不计费） */
    private Double estimatedFee = 0.0;

    /** 实际使用机时（小时，归还/结束时登记，用于机时利用率与结算统计） */
    private Double usedHours;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

package com.example.borrow.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_borrow_record")
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物资ID */
    @Column(nullable = false)
    private Long materialId;

    /** 物资名称(冗余，方便查询) */
    @Column(length = 100)
    private String materialName;

    /** 物资类别(冗余) */
    @Column(length = 20)
    private String materialCategory;

    /** 借用人姓名 */
    @Column(length = 50, nullable = false)
    private String borrower;

    /**
     * 借用人用户名。
     * 业务归属和权限判断必须使用稳定的用户名，不能使用可修改、可重名的真实姓名。
     * 历史数据该字段为空时，由启动任务按姓名回填。
     */
    @Column(length = 50)
    private String borrowerUsername;

    /** 批量申请单号：同一实验一次提交的多项物资共用，审批时可一次处理 */
    @Column(length = 50)
    private String requestNo;

    /** 借用数量 */
    @Column(nullable = false)
    private Integer borrowQuantity;

    /**
     * 已归还数量（支持部分归还：多次归还累加，等于借用数量时整单结单）
     * 注意：新列不加 nullable=false，避免主库已有数据执行 alter 时失败；代码里 null 视为 0
     */
    @Column
    private Integer returnedQuantity = 0;

    /** 状态：PENDING-待审批，APPROVED-已通过，REJECTED-已拒绝，BORROWED-借用中，PENDING_EXTEND-待审批延期，OVERDUE-已逾期，RETURNED-已归还，DAMAGED-损坏 */
    @Column(length = 20, nullable = false)
    private String status;

    /** 借用时间 */
    @Column(nullable = false)
    private LocalDateTime borrowTime;

    /** 预计归还时间 */
    private LocalDateTime expectedReturnTime;

    /** 实际归还时间 */
    private LocalDateTime actualReturnTime;

    /** 用途说明 */
    @Column(length = 500)
    private String purpose;

    /** 归还备注 */
    @Column(length = 500)
    private String returnRemark;

    /** 是否损坏 */
    private Boolean isDamaged = false;

    /** 损坏描述 */
    @Column(length = 500)
    private String damageDescription;

    /** 审批人 */
    @Column(length = 50)
    private String approver;

    /** 审批时间 */
    private LocalDateTime approveTime;

    /** 拒绝原因 */
    @Column(length = 500)
    private String rejectReason;

    /**
     * 审批环节：MENTOR-待导师（实验室负责人）审批，STAFF-待教师/管理员终审。
     * 借用人配置了导师时先走第一环节，导师通过后再进入终审；未配置导师时直接为 STAFF。
     */
    @Column(length = 20)
    private String approvalStage;

    /** 导师审批人 / 审批时间 */
    @Column(length = 50)
    private String mentorApprover;
    private LocalDateTime mentorApproveTime;

    /** 预估费用（借用按天×单价、领用按件×单价；0 表示该物资不计费） */
    private Double estimatedFee = 0.0;

    /** 危险化学品领用登记的第二领用人（双人领用） */
    @Column(length = 50)
    private String secondBorrower;

    /** 申请延期天数 */
    private Integer extensionDays;

    /** 延期理由 */
    @Column(length = 500)
    private String extensionReason;

    /** 延期是否已批准 */
    private Boolean extensionApproved = false;

    /** 超期天数 */
    private Integer overdueDays;

    /** 罚款金额 */
    private Double fineAmount = 0.0;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

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

    /** 借用数量 */
    @Column(nullable = false)
    private Integer borrowQuantity;

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

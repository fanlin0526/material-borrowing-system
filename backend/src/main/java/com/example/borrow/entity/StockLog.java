package com.example.borrow.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 库存流水：每一次库存变动（入库/借出/归还/领用/调整）都记一条
 * 变动类型 changeType: IN-入库, OUT_BORROW-借出, RETURN-归还, CONSUME-领用核销, ADJUST-手工调整
 */
@Data
@Entity
@Table(name = "t_stock_log")
public class StockLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物资ID */
    @Column(nullable = false)
    private Long materialId;

    /** 物资名称(冗余) */
    @Column(length = 100)
    private String materialName;

    /** 变动类型 */
    @Column(length = 30, nullable = false)
    private String changeType;

    /** 变动数量（正=增加，负=减少） */
    @Column(nullable = false)
    private Integer changeQuantity;

    /** 变动前可借数量 */
    private Integer beforeQuantity;

    /** 变动后可借数量 */
    private Integer afterQuantity;

    /** 操作人 */
    @Column(length = 50)
    private String operator;

    /** 关联单据类型：BORROW-借用记录, STOCK_IN-入库单, MANUAL-手工 */
    @Column(length = 30)
    private String refType;

    /** 关联单据ID */
    private Long refId;

    /** 备注 */
    @Column(length = 200)
    private String remark;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

package com.example.borrow.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 采购入库单（主表）：物资从"凭空录入"改为"采购入库"环节
 */
@Data
@Entity
@Table(name = "t_stock_in")
public class StockIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 入库单号（IN+时间戳） */
    @Column(unique = true, length = 50)
    private String inNo;

    /** 供应商 */
    @Column(length = 100)
    private String supplier;

    /** 经办人 */
    @Column(length = 50)
    private String operator;

    /** 入库物资总数量（明细合计） */
    @Column(nullable = false)
    private Integer totalQuantity = 0;

    /** 入库总金额（明细合计） */
    private Double totalAmount = 0.0;

    /** 备注 */
    @Column(length = 500)
    private String remark;

    /**
     * 单据状态：NORMAL-正常，REVOKED-已撤销。
     * 撤销采用「留痕」方式：单据与明细保留，仅标记状态并把库存回退，
     * 便于事后追查「这单为什么少了 / 谁撤的」。历史数据该字段可能为 null，按正常处理。
     */
    @Column(length = 20)
    private String status = "NORMAL";

    /** 撤销人（仅已撤销的单据有值） */
    @Column(length = 50)
    private String revokeOperator;

    /** 撤销时间 */
    private LocalDateTime revokeTime;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

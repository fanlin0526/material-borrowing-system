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

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

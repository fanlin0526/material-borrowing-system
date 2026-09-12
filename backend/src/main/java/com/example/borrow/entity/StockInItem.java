package com.example.borrow.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 采购入库单明细：一行一种物资
 * materialId 为空表示入库单中直接新建的物资
 */
@Data
@Entity
@Table(name = "t_stock_in_item")
public class StockInItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属入库单ID */
    @Column(nullable = false)
    private Long stockInId;

    /** 物资ID（已存在的物资） */
    private Long materialId;

    /** 物资名称 */
    @Column(length = 100, nullable = false)
    private String materialName;

    /** 物资类别（DEVICE/CONSUMABLE/WATER/REAGENT/GLASSWARE） */
    @Column(length = 20)
    private String category;

    /** 规格型号 */
    @Column(length = 100)
    private String specification;

    /** 计量单位 */
    @Column(length = 20)
    private String unit;

    /** 入库数量 */
    @Column(nullable = false)
    private Integer quantity;

    /** 单价 */
    private Double unitPrice = 0.0;
}

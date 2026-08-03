package com.example.borrow.entity;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_material")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物资编号 */
    @Column(unique = true, length = 50)
    private String code;

    /** 物资名称 */
    @NotBlank(message = "物资名称不能为空")
    @Column(length = 100, nullable = false)
    private String name;

    /**
     * 大类：DEVICE-实验设备，CONSUMABLE-耗材，WATER-实验用水，REAGENT-化学试剂，GLASSWARE-玻璃器皿
     */
    @NotBlank(message = "物资类别不能为空")
    @Column(length = 20, nullable = false)
    private String category;

    /** 规格型号 */
    @Column(length = 100)
    private String specification;

    /** 计量单位 */
    @Column(length = 20)
    private String unit;

    /** 总数量 */
    @Min(value = 0, message = "数量不能为负数")
    @Column(nullable = false)
    private Integer totalQuantity;

    /** 当前可借数量 */
    @Column(nullable = false)
    private Integer availableQuantity;

    /** 库存预警阈值（可借数量低于此值预警） */
    @Column
    private Integer warningThreshold = 3;

    /** 存放位置 */
    @Column(length = 100)
    private String location;

    /** 备注 */
    @Column(length = 500)
    private String remark;

    /** 设备状态：AVAILABLE-可用，MAINTENANCE-维修中，SCRAPPED-已报废 */
    @Column(length = 20)
    private String deviceStatus = "AVAILABLE";

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /** 判断该类别是否为可归还型（设备/玻璃器皿可归还，耗材/水/试剂为消耗型） */
    public static boolean isReturnable(String category) {
        return "DEVICE".equals(category) || "GLASSWARE".equals(category);
    }
}

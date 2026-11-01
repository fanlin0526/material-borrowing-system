package com.example.borrow.entity;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_material")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 乐观锁版本号，防止并发审批导致库存被覆盖或超卖 */
    @Version
    private Long version;

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

    /**
     * 是否允许带离实验室借用（null 视为允许，兼容历史数据）。
     * 关闭后该物资只保留「预约」通道 —— 对应真实系统里「大型/固定式仪器只排机时、不外借」的做法。
     */
    private Boolean borrowable = true;

    /** 是否需要先通过培训授权才能预约 / 借用（null 视为不需要） */
    private Boolean needsTraining = false;

    /**
     * 借用是否需要审批（null 视为不需要）。
     * 默认关闭：普通耗材与常规设备登记即走，避免给师生增加无谓流程；
     * 打开后即使是教师 / 超管本人借用也要走审批 —— 用于高值、稀缺、收费或唯一一台的仪器，
     * 审批审的是配额与责任，而不是信任度。
     */
    private Boolean approvalRequired = false;

    /** 预约计费单价（元/小时，0 或不填表示不收费） */
    private Double hourlyPrice = 0.0;

    /** 借用 / 领用计费单价（元/件·天，0 或不填表示不收费） */
    private Double unitPrice = 0.0;

    /** 是否危险化学品 / 管制类物资（null 视为否）：领用需登记第二领用人，并按危化品口径统计 */
    private Boolean hazardous = false;

    /** CAS 登记号（危险化学品） */
    @Column(length = 50)
    private String casNo;

    /** 有效期（试剂 / 耗材）：临期与过期在物资列表中预警 */
    private LocalDate validUntil;

    /** 存放柜位（如「危化品柜 A-2」） */
    @Column(length = 50)
    private String cabinetNo;

    /** 下次检定 / 维护日期：过期后禁止预约与借用，需先维护再更新该日期 */
    private LocalDate nextMaintenanceDate;

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        // 历史库中可能存在 version 为 null 的物资记录；乐观锁字段为空时，
        // Hibernate 在第一次库存变更时会直接空指针。新记录统一从 0 开始。
        if (version == null) {
            version = 0L;
        }
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

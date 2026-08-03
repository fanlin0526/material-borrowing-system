package com.example.borrow.entity;

import lombok.Data;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分类编码 (DEVICE/CONSUMABLE/WATER/REAGENT/GLASSWARE) */
    @NotBlank(message = "分类编码不能为空")
    @Column(unique = true, length = 30, nullable = false)
    private String code;

    /** 分类名称 (实验设备/耗材/实验用水/化学试剂/玻璃器皿) */
    @NotBlank(message = "分类名称不能为空")
    @Column(length = 50, nullable = false)
    private String name;

    /** 描述 */
    @Column(length = 200)
    private String description;

    /** 排序 */
    @Column
    private Integer sortOrder = 0;

    /** 是否可归还（设备/玻璃器皿可归还，耗材/水/试剂为消耗型） */
    @Column(nullable = false)
    private Boolean returnable = false;

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
}

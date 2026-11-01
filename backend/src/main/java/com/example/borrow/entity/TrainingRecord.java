package com.example.borrow.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 培训 / 上机资质授权记录。
 *
 * 真实实验室里「没通过培训的人约不了大型仪器」是硬规则，本表就是这条规则的依据：
 * 物资标记 needsTraining=true 时，必须存在一条未过期的授权记录，才允许预约或借用。
 */
@Data
@Entity
@Table(name = "t_training_record")
public class TrainingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 物资ID（资质是按物资/仪器授权的） */
    @Column(nullable = false)
    private Long materialId;

    /** 物资名称（冗余，便于列表直接展示） */
    @Column(length = 100)
    private String materialName;

    /** 被授权人用户名 */
    @Column(length = 50, nullable = false)
    private String username;

    /** 被授权人姓名 */
    @Column(length = 50)
    private String realName;

    /** 培训讲师 / 考核人 */
    @Column(length = 50)
    private String trainer;

    /** 培训（考核）日期 */
    private LocalDate trainDate;

    /** 资质有效期：为空表示长期有效 */
    private LocalDate validUntil;

    /** 培训内容 / 备注 */
    @Column(length = 500)
    private String remark;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

package com.example.borrow.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 附件（设备照片、说明书、入库发票/验收单等）。
 *
 * 真实实验室系统的设备档案都要求留证，本表用「关联类型 + 关联ID」的方式挂载，
 * 同一套接口可服务物资（MATERIAL）与入库单（STOCK_IN）。
 */
@Data
@Entity
@Table(name = "t_attachment")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联类型：MATERIAL-物资，STOCK_IN-入库单 */
    @Column(length = 20, nullable = false)
    private String refType;

    /** 关联对象ID */
    @Column(nullable = false)
    private Long refId;

    /** 原始文件名（下载时还原） */
    @Column(length = 200)
    private String fileName;

    /** 存储文件名（避免同名覆盖） */
    @Column(length = 200)
    private String storedName;

    /** 文件类型 */
    @Column(length = 100)
    private String contentType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 上传人 */
    @Column(length = 50)
    private String uploader;

    @Column(updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

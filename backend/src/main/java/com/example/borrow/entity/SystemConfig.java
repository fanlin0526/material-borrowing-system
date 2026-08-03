package com.example.borrow.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "t_system_config")
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 配置键 */
    @Column(length = 100, unique = true, nullable = false)
    private String configKey;

    /** 配置值 */
    @Column(length = 500)
    private String configValue;

    /** 配置描述 */
    @Column(length = 255)
    private String description;
}

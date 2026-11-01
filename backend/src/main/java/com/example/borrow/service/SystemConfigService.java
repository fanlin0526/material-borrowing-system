package com.example.borrow.service;

import com.example.borrow.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 系统配置读取服务
 *
 * 统一「配置项 → 业务规则」的读取口径。此前各业务类各写一套解析逻辑，
 * 同一条配置在不同模块可能表现不一致；收敛到这里后，任何模块读到的都是同一份值。
 *
 * 约定：值为 0 表示该项不启用，业务侧据此跳过校验，
 * 与前端提示语「0=不开启」保持一致。
 */
@Service
public class SystemConfigService {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    /** 读取整数配置；缺省或非法值返回 def */
    public int getInt(String key, int def) {
        return (int) Math.round(getDouble(key, def));
    }

    /** 读取小数配置（如 reserve_min_hours 支持 0.5 小时）；缺省或非法值返回 def */
    public double getDouble(String key, double def) {
        return systemConfigRepository.findByConfigKey(key)
                .map(c -> {
                    try {
                        return Double.parseDouble(c.getConfigValue());
                    } catch (Exception e) {
                        return def;
                    }
                })
                .orElse(def);
    }

    /**
     * 读取布尔开关配置。可接受 true/false、1/0、on/off、是/否；
     * 缺省或无法解析时返回 def。
     */
    public boolean getBoolean(String key, boolean def) {
        return systemConfigRepository.findByConfigKey(key)
                .map(c -> {
                    String v = c.getConfigValue();
                    if (v == null) return def;
                    v = v.trim().toLowerCase();
                    if (v.isEmpty()) return def;
                    if (v.equals("true") || v.equals("1") || v.equals("on") || v.equals("是") || v.equals("开启")) return true;
                    if (v.equals("false") || v.equals("0") || v.equals("off") || v.equals("否") || v.equals("关闭")) return false;
                    return def;
                })
                .orElse(def);
    }
}

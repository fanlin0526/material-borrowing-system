package com.example.borrow.config;

import com.example.borrow.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * JWT 配置：从 application.yml 读取 jwt.secret 并注入 JwtUtil
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret:#{null}}")
    private String secret;

    @PostConstruct
    public void init() {
        if (secret != null && !secret.trim().isEmpty()) {
            JwtUtil.setSecret(secret.trim());
        }
    }
}

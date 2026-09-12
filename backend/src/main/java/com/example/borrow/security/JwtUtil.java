package com.example.borrow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

public class JwtUtil {

    /**
     * JWT 签名密钥（HS256 需要 ≥256 bits）
     * 生产环境应通过 application.yml 的 jwt.secret 或环境变量 JWT_SECRET 覆盖。
     */
    private static final String DEFAULT_SECRET = "MaterialBorrowSystem2024SecretKeyForJWT!!_32bytes";
    private static SecretKey KEY;

    static {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isEmpty()) {
            secret = System.getProperty("jwt.secret");
        }
        if (secret == null || secret.isEmpty()) {
            secret = DEFAULT_SECRET;
        }
        // Base64 编码后生成 HMAC-SHA256 密钥
        String encoded = Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
        KEY = Keys.hmacShaKeyFor(encoded.getBytes(StandardCharsets.UTF_8));
    }

    /** 允许 Spring 配置类在启动时注入 application.yml 中的 jwt.secret */
    public static void setSecret(String secret) {
        if (secret != null && !secret.isEmpty()) {
            String encoded = Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
            KEY = Keys.hmacShaKeyFor(encoded.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static final long EXPIRATION = 24 * 60 * 60 * 1000L; // 24小时

    /** 生成 Token */
    public static String generate(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    /** 解析 Token */
    public static Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** 校验 Token 是否有效 */
    public static boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 从 Token 中获取用户名 */
    public static String getUsername(String token) {
        return parse(token).getSubject();
    }

    /** 从 Token 中获取角色 */
    public static String getRole(String token) {
        return parse(token).get("role", String.class);
    }
}

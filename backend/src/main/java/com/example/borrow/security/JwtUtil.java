package com.example.borrow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
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

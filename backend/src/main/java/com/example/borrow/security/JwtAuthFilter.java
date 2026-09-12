package com.example.borrow.security;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // 放行路径
    private static final String[] SKIP_PATHS = {
        "/api/auth/login",
        "/api/auth/register",
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 放行 OPTIONS 预检
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 放行登录/注册
        for (String skip : SKIP_PATHS) {
            if (path.equals(skip)) {
                UserContext.clear(); // 防止 ThreadLocal 残留
                chain.doFilter(request, response);
                return;
            }
        }

        // 从 Header 获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (JwtUtil.validate(token)) {
                String role = JwtUtil.getRole(token);

                // 管理员接口角色校验：仅 SUPER_ADMIN 可访问 /api/admin/*
                if (path.startsWith("/api/admin/") && !"SUPER_ADMIN".equals(role)) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅管理员可访问\"}");
                    return;
                }

                UserContext.set(JwtUtil.getUsername(token), role);
                try {
                    chain.doFilter(request, response);
                } finally {
                    UserContext.clear();
                }
                return;
            }
        }

        // 未授权
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\"}");
    }
}

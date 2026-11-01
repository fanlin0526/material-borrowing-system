package com.example.borrow.security;

import com.example.borrow.entity.User;
import com.example.borrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    // 放行路径
    private static final String[] SKIP_PATHS = {
        "/api/auth/login",
        "/api/auth/register",
    };

    /**
     * 路径前缀 → 所需角色（"STAFF" = 超级管理员或教师）
     *
     * 为什么在 Filter 里先拦一道，而不是只靠控制器方法内的校验：
     * Spring MVC 的参数绑定发生在方法体执行**之前**。未授权者只要把请求构造得格式不对
     * （例如给 Excel 导入接口发一个非 multipart 请求），框架就会先抛解析异常，
     * 方法体第一行的角色校验根本来不及执行——不仅看不到「权限不足」，
     * 还留下了一个可探测接口行为的缝隙。放在这里就在参数解析之前把门关上了。
     *
     * 控制器方法内仍保留同样的校验，作为第二道防线（双保险）。
     */
    private static final String[][] PATH_ROLES = {
        {"/api/admin/",  "SUPER_ADMIN"},
        {"/api/import/", "SUPER_ADMIN"},
        {"/api/export/", "STAFF"},
        {"/api/stock-in", "STAFF"},
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
                String username = JwtUtil.getUsername(token);
                User user = userRepository.findByUsername(username).orElse(null);
                // 每次请求都以数据库中的账号状态和角色为准：
                // 停用账号立即失效，角色调整后无需等待旧 Token 过期。
                if (user == null || Boolean.FALSE.equals(user.getEnabled())) {
                    denyUnauthorized(response, user == null ? "账号不存在" : "账号已停用");
                    return;
                }
                String role = user.getRole();

                // 路径级角色校验：仅 SUPER_ADMIN 可访问 /api/admin/*、/api/import/*；
                // 教师与超管可访问 /api/export/*、/api/stock-in/*
                String required = requiredRole(path);
                if (required != null && !hasRole(role, required)) {
                    deny(response, required);
                    return;
                }

                UserContext.set(user.getUsername(), role, user.getRealName());
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

    /** 该路径要求的角色；返回 null 表示不限制（登录即可） */
    private String requiredRole(String path) {
        for (String[] rule : PATH_ROLES) {
            if (path.startsWith(rule[0])) {
                return rule[1];
            }
        }
        return null;
    }

    private boolean hasRole(String role, String required) {
        if ("SUPER_ADMIN".equals(required)) {
            return "SUPER_ADMIN".equals(role);
        }
        if ("STAFF".equals(required)) {
            return "SUPER_ADMIN".equals(role) || "TEACHER".equals(role);
        }
        return true;
    }

    private void deny(HttpServletResponse response, String required) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(403);
        response.getWriter().write("{\"code\":403,\"message\":\""
                + ("SUPER_ADMIN".equals(required) ? "权限不足，仅管理员可访问" : "权限不足，仅教师或管理员可访问")
                + "\"}");
    }

    private void denyUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }
}

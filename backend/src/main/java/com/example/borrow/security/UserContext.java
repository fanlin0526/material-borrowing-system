package com.example.borrow.security;

/**
 * 当前登录用户上下文（ThreadLocal）
 *
 * 只存「服务端从 Token 解出的身份」，不存任何来自请求体的字段。
 * 业务代码要写"谁做了这件事"，一律从这里取，不要从 body 取。
 */
public class UserContext {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_ROLE = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_REALNAME = new ThreadLocal<>();

    public static void set(String username, String role) {
        set(username, role, null);
    }

    public static void set(String username, String role, String realName) {
        CURRENT_USER.set(username);
        CURRENT_ROLE.set(role);
        // 旧 Token 不含 realName，回退到用户名，保证「操作人」字段不为空
        CURRENT_REALNAME.set(realName != null && !realName.isEmpty() ? realName : username);
    }

    public static String getUsername() {
        return CURRENT_USER.get();
    }

    public static String getRole() {
        return CURRENT_ROLE.get();
    }

    /** 当前登录人的展示名（真实姓名，缺失时回退用户名） */
    public static String getRealName() {
        String name = CURRENT_REALNAME.get();
        return name != null ? name : CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
        CURRENT_ROLE.remove();
        CURRENT_REALNAME.remove();
    }
}

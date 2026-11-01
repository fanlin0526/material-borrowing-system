package com.example.borrow.security;

/**
 * 服务端角色守卫
 *
 * 所有权限判断都放在服务端，不依赖前端隐藏按钮——前端按钮只是体验，
 * 真正拦住越权请求的是这里。各控制器统一调用，避免每个类各写一套判断口径。
 *
 * 角色：SUPER_ADMIN-超级管理员，TEACHER-教师，STUDENT-学生
 */
public class RoleGuard {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String TEACHER = "TEACHER";

    /** 仅超级管理员：基础数据维护（物资建档/改档/删除、分类管理、Excel 批量导入） */
    public static void requireSuperAdmin() {
        if (!SUPER_ADMIN.equals(UserContext.getRole())) {
            throw new RuntimeException("权限不足：该操作仅超级管理员可执行");
        }
    }

    /** 教师或超级管理员：日常业务操作（审批、采购入库、导出、物资状态维护） */
    public static void requireStaff() {
        if (!isStaff()) {
            throw new RuntimeException("权限不足：该操作仅教师或管理员可执行");
        }
    }

    public static boolean isStaff() {
        String role = UserContext.getRole();
        return SUPER_ADMIN.equals(role) || TEACHER.equals(role);
    }

    public static boolean isSuperAdmin() {
        return SUPER_ADMIN.equals(UserContext.getRole());
    }

    /** 当前登录人的显示名（真实姓名，缺失时回退用户名），用于写入操作人字段 */
    public static String currentOperator() {
        String name = UserContext.getRealName();
        return name != null ? name : "未知用户";
    }
}

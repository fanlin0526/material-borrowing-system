package com.example.borrow.controller;

import com.example.borrow.entity.Reservation;
import com.example.borrow.security.RoleGuard;
import com.example.borrow.security.UserContext;
import com.example.borrow.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    /** 创建单次预约（提交后为待审批，需教师/管理员审批） */
    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        Long materialId = Long.valueOf(body.get("materialId").toString());
        // 预约人取自登录态，不取请求体：请求体可被伪造成他人名义预约
        String username = UserContext.getUsername();
        String realName = UserContext.getRealName();
        LocalDateTime startTime = LocalDateTime.parse(body.get("startTime").toString());
        LocalDateTime endTime = LocalDateTime.parse(body.get("endTime").toString());
        String purpose = (String) body.get("purpose");
        Integer attendeeCount = body.get("attendeeCount") != null
                ? Integer.valueOf(body.get("attendeeCount").toString()) : 1;

        return Result.ok(reservationService.createReservation(
                materialId, username, realName, startTime, endTime, purpose, attendeeCount,
                RoleGuard.isStaff()));
    }

    /** 创建周期性预约（展开为多条待审批预约） */
    @PostMapping("/recurring")
    public Result<?> createRecurring(@RequestBody Map<String, Object> body) {
        Long materialId = Long.valueOf(body.get("materialId").toString());
        String username = UserContext.getUsername();
        String realName = UserContext.getRealName();
        LocalDateTime firstStart = LocalDateTime.parse(body.get("firstStart").toString());
        LocalDateTime firstEnd = LocalDateTime.parse(body.get("firstEnd").toString());
        String recurringRule = (String) body.get("recurringRule");
        int weeks = Integer.parseInt(body.get("weeks").toString());
        String purpose = (String) body.get("purpose");

        return Result.ok(reservationService.createRecurring(
                materialId, username, realName, firstStart, firstEnd, recurringRule, weeks, purpose,
                RoleGuard.isStaff()));
    }

    /** 实时冲突检测 */
    @PostMapping("/check-conflict")
    public Result<?> checkConflict(@RequestBody Map<String, Object> body) {
        Long materialId = Long.valueOf(body.get("materialId").toString());
        LocalDateTime startTime = LocalDateTime.parse(body.get("startTime").toString());
        LocalDateTime endTime = LocalDateTime.parse(body.get("endTime").toString());
        return Result.ok(reservationService.checkConflict(materialId, startTime, endTime));
    }

    /** 获取某周的总览 */
    @GetMapping("/week")
    public Result<?> weekOverview(@RequestParam String from, @RequestParam String to) {
        LocalDateTime weekStart = LocalDateTime.parse(from);
        LocalDateTime weekEnd = LocalDateTime.parse(to);
        return Result.ok(reservationService.getWeekOverview(weekStart, weekEnd));
    }

    /** 获取某物资某周的预约 */
    @GetMapping("/week/{materialId}")
    public Result<?> weekReservations(@PathVariable Long materialId,
                                       @RequestParam String from, @RequestParam String to) {
        LocalDateTime weekStart = LocalDateTime.parse(from);
        LocalDateTime weekEnd = LocalDateTime.parse(to);
        return Result.ok(reservationService.getWeekReservations(materialId, weekStart, weekEnd));
    }

    // ==================== 审批（教师 / 超级管理员） ====================

    /** 待审批预约列表（审批页签 + 角标） */
    @GetMapping("/pending")
    public Result<?> pending() {
        requireApprover();
        return Result.ok(reservationService.pendingList());
    }

    /** 全部预约列表（可按状态筛选，教师/管理员查看） */
    @GetMapping("/all")
    public Result<?> all(@RequestParam(required = false) String status) {
        requireApprover();
        return Result.ok(reservationService.listAll(status));
    }

    /** 审批通过：生成借用记录（可归还型→借用中；消耗型→已领用），闭环到归还 */
    @PutMapping("/approve/{id}")
    public Result<?> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        requireApprover();
        return Result.ok(reservationService.approve(id, RoleGuard.currentOperator()));
    }

    /** 审批拒绝 */
    @PutMapping("/reject/{id}")
    public Result<?> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        requireApprover();
        String reason = body != null ? body.get("reason") : null;
        reservationService.reject(id, RoleGuard.currentOperator(), reason);
        return Result.ok("已拒绝该预约");
    }

    // ==================== 用户操作 ====================

    /** 取消预约（仅待审批状态可取消；本人或教师/管理员） */
    @PutMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        Reservation r = reservationService.getById(id);
        if (!RoleGuard.isStaff()) {
            String me = UserContext.getUsername();
            if (r.getUsername() == null || me == null || !r.getUsername().equals(me)) {
                throw new RuntimeException("权限不足：只能取消自己的预约");
            }
        }
        reservationService.cancel(id);
        return Result.ok();
    }

    /** 我的预约列表（取登录态，不接受前端传入用户名） */
    @GetMapping("/my")
    public Result<?> myReservations() {
        String username = UserContext.getUsername();
        if (username == null) throw new RuntimeException("未登录");
        return Result.ok(reservationService.listByUser(username));
    }

    // ==================== 权限校验 ====================

    /** 审批类接口仅教师 / 超级管理员可访问（服务端校验，不依赖前端隐藏按钮） */
    private void requireApprover() {
        RoleGuard.requireStaff();
    }
}

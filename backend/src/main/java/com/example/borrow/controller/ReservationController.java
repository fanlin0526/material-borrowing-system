package com.example.borrow.controller;

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

    /** 创建单次预约 */
    @PostMapping
    public Result<?> create(@RequestBody Map<String, Object> body) {
        Long materialId = Long.valueOf(body.get("materialId").toString());
        String username = (String) body.get("username");
        String realName = (String) body.get("realName");
        LocalDateTime startTime = LocalDateTime.parse(body.get("startTime").toString());
        LocalDateTime endTime = LocalDateTime.parse(body.get("endTime").toString());
        String purpose = (String) body.get("purpose");
        Integer attendeeCount = body.get("attendeeCount") != null
                ? Integer.valueOf(body.get("attendeeCount").toString()) : 1;

        return Result.ok(reservationService.createReservation(
                materialId, username, realName, startTime, endTime, purpose, attendeeCount));
    }

    /** 创建周期性预约 */
    @PostMapping("/recurring")
    public Result<?> createRecurring(@RequestBody Map<String, Object> body) {
        Long materialId = Long.valueOf(body.get("materialId").toString());
        String username = (String) body.get("username");
        String realName = (String) body.get("realName");
        LocalDateTime firstStart = LocalDateTime.parse(body.get("firstStart").toString());
        LocalDateTime firstEnd = LocalDateTime.parse(body.get("firstEnd").toString());
        String recurringRule = (String) body.get("recurringRule");
        int weeks = Integer.parseInt(body.get("weeks").toString());
        String purpose = (String) body.get("purpose");

        return Result.ok(reservationService.createRecurring(
                materialId, username, realName, firstStart, firstEnd, recurringRule, weeks, purpose));
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

    /** 获取某设备某周的预约 */
    @GetMapping("/week/{materialId}")
    public Result<?> weekReservations(@PathVariable Long materialId,
                                       @RequestParam String from, @RequestParam String to) {
        LocalDateTime weekStart = LocalDateTime.parse(from);
        LocalDateTime weekEnd = LocalDateTime.parse(to);
        return Result.ok(reservationService.getWeekReservations(materialId, weekStart, weekEnd));
    }

    /** 取消预约 */
    @PutMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return Result.ok();
    }

    /** 用户预约列表 */
    @GetMapping("/my")
    public Result<?> myReservations(@RequestParam String username) {
        return Result.ok(reservationService.listByUser(username));
    }
}

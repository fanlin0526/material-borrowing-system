package com.example.borrow.controller;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.entity.Material;
import com.example.borrow.entity.Reservation;
import com.example.borrow.entity.User;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.repository.ReservationRepository;
import com.example.borrow.repository.UserRepository;
import com.example.borrow.security.RoleGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 运营分析接口（教师 / 超管）：按部门统计、费用汇总、机时利用率。
 *
 * 这三项是真实实验室共享平台年度考核的常规口径：
 *  - 按部门（学院/课题组）统计借用与预约量，用于分摊与考核；
 *  - 费用汇总用于有偿使用结算；
 *  - 机时利用率 = 实际使用机时 / 可开放机时，替代此前自造的「库存健康度」。
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    /** 机时利用率统计窗口（天） */
    private static final int WINDOW_DAYS = 30;
    /** 每日开放机时（与预约窗口 7:00-21:00 一致） */
    private static final double OPEN_HOURS_PER_DAY = 14.0;

    @Autowired private BorrowRecordRepository borrowRecordRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public Result<?> overview() {
        RoleGuard.requireStaff();

        List<BorrowRecord> borrows = borrowRecordRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAll();
        List<Material> materials = materialRepository.findAll();
        List<User> users = userRepository.findAll();

        // 用户名 → 部门；历史借用记录没有用户名时才回退到姓名
        Map<String, String> deptOfUsername = new HashMap<>();
        Map<String, String> deptOfName = new HashMap<>();
        for (User u : users) {
            String dept = u.getDepartment() != null ? u.getDepartment() : "未设置";
            if (u.getUsername() != null) deptOfUsername.put(u.getUsername(), dept);
            if (u.getRealName() != null) {
                deptOfName.put(u.getRealName(), dept);
            }
        }

        Map<String, Integer> borrowByDept = new TreeMap<>();
        Map<String, Integer> reserveByDept = new TreeMap<>();
        for (BorrowRecord r : borrows) {
            String d = r.getBorrowerUsername() != null
                    ? deptOfUsername.getOrDefault(r.getBorrowerUsername(), "未设置")
                    : deptOfName.getOrDefault(r.getBorrower(), "未设置");
            borrowByDept.merge(d, 1, Integer::sum);
        }
        for (Reservation r : reservations) {
            String d = deptOfUsername.getOrDefault(r.getUsername(),
                    deptOfName.getOrDefault(r.getRealName(), "未设置"));
            reserveByDept.merge(d, 1, Integer::sum);
        }
        Set<String> depts = new LinkedHashSet<>();
        depts.addAll(borrowByDept.keySet());
        depts.addAll(reserveByDept.keySet());
        List<Map<String, Object>> byDept = new ArrayList<>();
        for (String d : depts) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("department", d);
            row.put("borrowCount", borrowByDept.getOrDefault(d, 0));
            row.put("reserveCount", reserveByDept.getOrDefault(d, 0));
            byDept.add(row);
        }

        // 费用汇总（预估口径：借用按天/件 × 单价，预约按机时 × 单价）
        double borrowFee = borrows.stream()
                .mapToDouble(r -> r.getEstimatedFee() != null ? r.getEstimatedFee() : 0).sum();
        double reserveFee = reservations.stream()
                .mapToDouble(r -> r.getEstimatedFee() != null ? r.getEstimatedFee() : 0).sum();

        // 机时利用率：窗口内已通过的预约时长 / （设备数 × 窗口天数 × 每日开放机时）
        LocalDateTime from = LocalDateTime.now().minusDays(WINDOW_DAYS);
        LocalDateTime to = LocalDateTime.now().plusDays(WINDOW_DAYS);
        Map<Long, Double> usedHoursByMaterial = new HashMap<>();
        for (Reservation r : reservations) {
            if (!"APPROVED".equals(r.getStatus())) continue;
            if (r.getStartTime() == null || r.getEndTime() == null) continue;
            if (r.getEndTime().isBefore(LocalDateTime.now().minusDays(WINDOW_DAYS))) continue;
            if (r.getStartTime().isAfter(to)) continue;
            double hours = Duration.between(r.getStartTime(), r.getEndTime()).toMinutes() / 60.0;
            usedHoursByMaterial.merge(r.getMaterialId(), hours, Double::sum);
        }
        List<Map<String, Object>> utilization = new ArrayList<>();
        for (Material m : materials) {
            if (!"DEVICE".equals(m.getCategory())) continue;
            double used = usedHoursByMaterial.getOrDefault(m.getId(), 0.0);
            // 单台设备的可开放机时按窗口天数 × 每日开放时长计
            double open = WINDOW_DAYS * OPEN_HOURS_PER_DAY;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", m.getName());
            row.put("usedHours", Math.round(used * 10) / 10.0);
            row.put("openHours", (int) open);
            row.put("rate", open > 0 ? Math.round(used / open * 1000) / 10.0 : 0.0);
            utilization.add(row);
        }
        utilization.sort((a, b) -> Double.compare((double) b.get("rate"), (double) a.get("rate")));
        if (utilization.size() > 8) utilization = utilization.subList(0, 8);

        // 设备完好率：可用（含在借）与维修中的设备占比，报废不计入
        long totalDevice = 0, intact = 0;
        for (Material m : materials) {
            if (!"DEVICE".equals(m.getCategory()) && !"GLASSWARE".equals(m.getCategory())) continue;
            if ("SCRAPPED".equals(m.getDeviceStatus())) continue;
            totalDevice++;
            if (!"MAINTENANCE".equals(m.getDeviceStatus())) intact++;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("windowDays", WINDOW_DAYS);
        data.put("byDepartment", byDept);
        data.put("borrowFee", Math.round(borrowFee * 100) / 100.0);
        data.put("reserveFee", Math.round(reserveFee * 100) / 100.0);
        data.put("totalFee", Math.round((borrowFee + reserveFee) * 100) / 100.0);
        data.put("utilization", utilization);
        data.put("intactRate", totalDevice > 0 ? Math.round(intact * 1000.0 / totalDevice) / 10.0 : 0.0);
        data.put("totalDevice", totalDevice);
        data.put("intactDevice", intact);
        data.put("generatedAt", LocalDate.now().toString());
        return Result.ok(data);
    }
}

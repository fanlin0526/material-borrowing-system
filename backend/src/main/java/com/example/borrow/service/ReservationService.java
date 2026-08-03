package com.example.borrow.service;

import com.example.borrow.entity.Material;
import com.example.borrow.entity.Reservation;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MaterialRepository materialRepository;

    /**
     * 创建单次预约
     */
    @Transactional
    public Reservation createReservation(Long materialId, String username, String realName,
                                          LocalDateTime startTime, LocalDateTime endTime,
                                          String purpose, Integer attendeeCount) {
        // 1. 校验物资是否存在
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("物资不存在"));

        // 2. 设备状态校验
        if ("MAINTENANCE".equals(material.getDeviceStatus())) {
            throw new RuntimeException("该设备正在维修中，暂不可预约");
        }
        if ("SCRAPPED".equals(material.getDeviceStatus())) {
            throw new RuntimeException("该设备已报废，不可预约");
        }

        // 3. 时间段合法性
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new RuntimeException("开始时间必须早于结束时间");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("不能预约过去的时间");
        }

        // 4. 冲突检测
        List<Reservation> conflicts = reservationRepository.findConflicting(materialId, startTime, endTime);
        if (!conflicts.isEmpty()) {
            Reservation c = conflicts.get(0);
            throw new RuntimeException("时段冲突：该设备在 "
                    + c.getStartTime().toLocalDate() + " "
                    + c.getStartTime().toLocalTime() + " ~ "
                    + c.getEndTime().toLocalTime()
                    + " 已被「" + c.getRealName() + "」预约");
        }

        // 5. 创建预约（演示系统直接 APPROVED）
        Reservation r = new Reservation();
        r.setMaterialId(materialId);
        r.setMaterialName(material.getName());
        r.setMaterialCategory(material.getCategory());
        r.setUsername(username);
        r.setRealName(realName);
        r.setStartTime(startTime);
        r.setEndTime(endTime);
        r.setType("SINGLE");
        r.setStatus("APPROVED");
        r.setPurpose(purpose);
        r.setAttendeeCount(attendeeCount);
        return reservationRepository.save(r);
    }

    /**
     * 创建周期性预约（自动展开未来 N 周）
     */
    @Transactional
    public List<Reservation> createRecurring(Long materialId, String username, String realName,
                                              LocalDateTime firstStart, LocalDateTime firstEnd,
                                              String recurringRule, int weeks,
                                              String purpose) {
        List<Reservation> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int w = 0; w < weeks; w++) {
            LocalDateTime s = firstStart.plusWeeks(w * getWeekMultiplier(recurringRule));
            LocalDateTime e = firstEnd.plusWeeks(w * getWeekMultiplier(recurringRule));
            try {
                created.add(createReservation(materialId, username, realName, s, e,
                        purpose + "（第" + (w + 1) + "次）", 1));
            } catch (RuntimeException ex) {
                errors.add(ex.getMessage());
            }
        }

        if (created.isEmpty() && !errors.isEmpty()) {
            throw new RuntimeException("周期预约全部失败：" + String.join("；", errors));
        }

        // 更新类型标记
        for (Reservation r : created) {
            r.setType("RECURRING");
            r.setRecurringRule(recurringRule);
            reservationRepository.save(r);
        }

        return created;
    }

    private int getWeekMultiplier(String rule) {
        if ("BIWEEKLY".equals(rule)) return 2;
        if ("MONTHLY".equals(rule)) return 4;
        return 1; // WEEKLY or default
    }

    /**
     * 实时冲突检测（供前端实时校验）
     */
    public Map<String, Object> checkConflict(Long materialId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        List<Reservation> conflicts = reservationRepository.findConflicting(materialId, startTime, endTime);
        result.put("hasConflict", !conflicts.isEmpty());
        if (!conflicts.isEmpty()) {
            List<Map<String, Object>> conflictList = new ArrayList<>();
            for (Reservation c : conflicts) {
                Map<String, Object> item = new HashMap<>();
                item.put("startTime", c.getStartTime().toString());
                item.put("endTime", c.getEndTime().toString());
                item.put("reservedBy", c.getRealName());
                item.put("purpose", c.getPurpose());
                conflictList.add(item);
            }
            result.put("conflicts", conflictList);
        }
        return result;
    }

    /**
     * 获取某设备某周的预约（用于日历展示）
     */
    public List<Reservation> getWeekReservations(Long materialId, LocalDateTime weekStart, LocalDateTime weekEnd) {
        return reservationRepository.findByMaterialAndDateRange(materialId, weekStart, weekEnd);
    }

    /**
     * 获取所有物资某周的预约（用于日历总览）
     */
    public List<Map<String, Object>> getWeekOverview(LocalDateTime weekStart, LocalDateTime weekEnd) {
        List<Material> allMaterials = materialRepository.findAll();
        List<Map<String, Object>> overview = new ArrayList<>();

        for (Material m : allMaterials) {
            if (!"AVAILABLE".equals(m.getDeviceStatus())) continue;

            List<Reservation> reservations = reservationRepository
                    .findByMaterialAndDateRange(m.getId(), weekStart, weekEnd);
            if (!reservations.isEmpty()) {
                Map<String, Object> item = new HashMap<>();
                item.put("materialId", m.getId());
                item.put("materialName", m.getName());
                item.put("category", m.getCategory());
                item.put("reservations", reservations);
                overview.add(item);
            }
        }
        return overview;
    }

    /** 取消预约 */
    @Transactional
    public void cancel(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        r.setStatus("CANCELLED");
        reservationRepository.save(r);
    }

    /** 用户预约列表 */
    public List<Reservation> listByUser(String username) {
        return reservationRepository.findByUsernameOrderByCreateTimeDesc(username);
    }

    /** 全部预约列表 */
    public List<Reservation> listAll(String status) {
        if (status != null && !status.isEmpty())
            return reservationRepository.findByStatusOrderByCreateTimeDesc(status);
        return reservationRepository.findAll();
    }
}

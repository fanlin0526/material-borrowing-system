package com.example.borrow.controller;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.entity.Category;
import com.example.borrow.entity.Material;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.CategoryRepository;
import com.example.borrow.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 仪表盘综合数据：统计卡片 + 分类分布 + 近7天借用趋势 + 热门物资 + 最近记录
     */
    @GetMapping
    public Result<?> overview() {
        Map<String, Object> data = new LinkedHashMap<>();

        List<Material> allMaterials = materialRepository.findAll();
        List<BorrowRecord> allRecords = borrowRecordRepository.findAll();

        // ===== 1. 统计卡片 =====
        Map<String, Object> cards = new LinkedHashMap<>();
        cards.put("totalMaterials", allMaterials.size());

        long borrowing = allRecords.stream().filter(r -> "BORROWED".equals(r.getStatus())).count();
        cards.put("borrowingCount", borrowing);
        cards.put("totalRecords", allRecords.size());

        long lowStock = allMaterials.stream()
                .filter(m -> m.getAvailableQuantity() <= (m.getWarningThreshold() != null ? m.getWarningThreshold() : 3))
                .count();
        cards.put("lowStockCount", lowStock);
        data.put("cards", cards);

        // ===== 2. 分类分布（饼图，动态加载） =====
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        Map<String, Integer> categoryCount = new LinkedHashMap<>();
        Map<String, String> categoryNames = new LinkedHashMap<>();
        for (Category cat : categories) {
            categoryNames.put(cat.getCode(), cat.getName());
            categoryCount.put(cat.getCode(), 0);
        }
        for (Material m : allMaterials) {
            categoryCount.merge(m.getCategory(), 1, Integer::sum);
        }

        List<Map<String, Object>> pieData = new ArrayList<>();
        for (Map.Entry<String, String> entry : categoryNames.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getValue());
            item.put("value", categoryCount.get(entry.getKey()));
            pieData.add(item);
        }
        data.put("categoryPie", pieData);

        // ===== 3. 近7天借用趋势（柱状图） =====
        List<String> trendDates = new ArrayList<>();
        List<Integer> trendCounts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            String label = String.format("%02d-%02d", day.getMonthValue(), day.getDayOfMonth());
            trendDates.add(label);
            long count = allRecords.stream()
                    .filter(r -> r.getBorrowTime() != null && r.getBorrowTime().toLocalDate().equals(day))
                    .count();
            trendCounts.add((int) count);
        }
        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("dates", trendDates);
        trend.put("counts", trendCounts);
        data.put("borrowTrend", trend);

        // ===== 4. 热门借用物资 TOP5（柱状图） =====
        Map<String, Integer> borrowFreq = new LinkedHashMap<>();
        for (BorrowRecord r : allRecords) {
            borrowFreq.merge(r.getMaterialName(), 1, Integer::sum);
        }
        List<Map<String, Object>> topList = new ArrayList<>();
        borrowFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", e.getKey());
                    item.put("count", e.getValue());
                    topList.add(item);
                });
        data.put("topBorrowed", topList);

        // ===== 5. 最近借用记录 TOP8 =====
        List<BorrowRecord> recent = new ArrayList<>(allRecords);
        recent.sort((a, b) -> b.getBorrowTime().compareTo(a.getBorrowTime()));
        if (recent.size() > 8) recent = recent.subList(0, 8);
        data.put("recentRecords", recent);

        return Result.ok(data);
    }

    /**
     * 扩展数据分析（设备利用率、月度对比、库存健康度、分类消耗速度）
     */
    @GetMapping("/detail")
    public Result<?> detail() {
        Map<String, Object> data = new LinkedHashMap<>();

        List<Material> allMaterials = materialRepository.findAll();
        List<BorrowRecord> allRecords = borrowRecordRepository.findAll();

        // ===== 1. 设备使用率排名（按被借用次数 × 每次假设4小时 × 库存量折算） =====
        // 公式: 利用率 = 借用次数 / (库存总量 × 加权系数)
        Map<String, Integer> usageFreq = new LinkedHashMap<>();
        Map<String, Integer> usageQty = new LinkedHashMap<>();
        Map<String, String> matCat = new LinkedHashMap<>();
        for (BorrowRecord r : allRecords) {
            usageFreq.merge(r.getMaterialName(), 1, Integer::sum);
            usageQty.merge(r.getMaterialName(), r.getBorrowQuantity(), Integer::sum);
            matCat.putIfAbsent(r.getMaterialName(), r.getMaterialCategory());
        }
        // 只展示 DEVICE 类型（设备），按借用次数排名
        List<Map<String, Object>> deviceUsage = new ArrayList<>();
        for (Map.Entry<String, Integer> e : usageFreq.entrySet()) {
            if (!"DEVICE".equals(matCat.get(e.getKey()))) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", e.getKey());
            item.put("times", e.getValue());
            item.put("quantity", usageQty.getOrDefault(e.getKey(), 0));
            deviceUsage.add(item);
        }
        deviceUsage.sort((a, b) -> Integer.compare((int) b.get("times"), (int) a.get("times")));
        data.put("deviceUsage", deviceUsage);

        // ===== 2. 月度借用对比（本月 vs 上月） =====
        LocalDate now = LocalDate.now();
        LocalDate firstOfThisMonth = now.withDayOfMonth(1);
        LocalDate firstOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfLastMonth = firstOfThisMonth.minusDays(1);

        long thisMonthCount = allRecords.stream()
                .filter(r -> r.getBorrowTime() != null
                        && !r.getBorrowTime().toLocalDate().isBefore(firstOfThisMonth))
                .count();
        long lastMonthCount = allRecords.stream()
                .filter(r -> r.getBorrowTime() != null
                        && !r.getBorrowTime().toLocalDate().isBefore(firstOfLastMonth)
                        && !r.getBorrowTime().toLocalDate().isAfter(endOfLastMonth))
                .count();

        // 本月每日借用数
        List<Integer> thisMonthDaily = new ArrayList<>();
        List<String> monthDays = new ArrayList<>();
        int daysInMonth = now.lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate day = firstOfThisMonth.withDayOfMonth(Math.min(d, daysInMonth));
            monthDays.add(String.valueOf(d));
            long count = allRecords.stream()
                    .filter(r -> r.getBorrowTime() != null && r.getBorrowTime().toLocalDate().equals(day))
                    .count();
            thisMonthDaily.add((int) count);
        }

        Map<String, Object> monthlyComparison = new LinkedHashMap<>();
        monthlyComparison.put("thisMonth", thisMonthCount);
        monthlyComparison.put("lastMonth", lastMonthCount);
        monthlyComparison.put("thisMonthLabel", now.getMonthValue() + "月");
        monthlyComparison.put("lastMonthLabel", now.minusMonths(1).getMonthValue() + "月");
        monthlyComparison.put("dailyDates", monthDays);
        monthlyComparison.put("dailyCounts", thisMonthDaily);
        data.put("monthlyComparison", monthlyComparison);

        // ===== 3. 库存健康度 =====
        int totalItems = allMaterials.size();
        long healthyItems = allMaterials.stream()
                .filter(m -> {
                    int threshold = m.getWarningThreshold() != null ? m.getWarningThreshold() : 3;
                    return m.getAvailableQuantity() > threshold;
                }).count();
        long lowItems = allMaterials.stream()
                .filter(m -> {
                    int threshold = m.getWarningThreshold() != null ? m.getWarningThreshold() : 3;
                    return m.getAvailableQuantity() > 0 && m.getAvailableQuantity() <= threshold;
                }).count();
        long outOfStock = allMaterials.stream()
                .filter(m -> m.getAvailableQuantity() == 0).count();

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("totalItems", totalItems);
        health.put("healthyItems", (int) healthyItems);
        health.put("lowItems", (int) lowItems);
        health.put("outOfStock", (int) outOfStock);
        health.put("score", totalItems > 0 ? Math.round(healthyItems * 100.0 / totalItems) : 0);
        data.put("health", health);

        // ===== 4. 分类消耗速度 =====
        List<Category> detailCats = categoryRepository.findAllByOrderBySortOrderAsc();
        Map<String, Integer> catConsume = new LinkedHashMap<>();
        Map<String, Integer> catTotal = new LinkedHashMap<>();
        Map<String, String> catNames = new LinkedHashMap<>();
        for (Category cat : detailCats) {
            catNames.put(cat.getCode(), cat.getName());
            catConsume.put(cat.getCode(), 0);
            catTotal.put(cat.getCode(), 0);
        }
        for (Material m : allMaterials) {
            int borrowed = m.getTotalQuantity() - m.getAvailableQuantity();
            catConsume.merge(m.getCategory(), borrowed, Integer::sum);
            catTotal.merge(m.getCategory(), m.getTotalQuantity(), Integer::sum);
        }
        List<Map<String, Object>> consumeRate = new ArrayList<>();
        for (String cat : catNames.keySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", catNames.get(cat));
            item.put("consumed", catConsume.get(cat));
            item.put("total", catTotal.get(cat));
            item.put("rate", catTotal.get(cat) > 0
                    ? Math.round(catConsume.get(cat) * 100.0 / catTotal.get(cat))
                    : 0);
            consumeRate.add(item);
        }
        data.put("consumeRate", consumeRate);

        // ===== 5. 时段热力分布（最近 30 天按星期几+小时统计） =====
        Map<String, Map<Integer, Integer>> heatmap = new LinkedHashMap<>();
        String[] weekLabels = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (String w : weekLabels) {
            Map<Integer, Integer> hourMap = new LinkedHashMap<>();
            for (int h = 8; h <= 18; h++) hourMap.put(h, 0);
            heatmap.put(w, hourMap);
        }
        for (BorrowRecord r : allRecords) {
            if (r.getBorrowTime() == null) continue;
            // 检查是否在最近 30 天内
            if (r.getBorrowTime().isBefore(LocalDateTime.now().minusDays(30))) continue;
            int dayOfWeek = r.getBorrowTime().getDayOfWeek().getValue() - 1; // 0=Monday
            int hour = r.getBorrowTime().getHour();
            if (hour < 8 || hour > 18) continue;
            String weekLabel = weekLabels[dayOfWeek];
            heatmap.get(weekLabel).merge(hour, 1, Integer::sum);
        }
        data.put("heatmap", heatmap);

        return Result.ok(data);
    }
}

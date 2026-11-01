package com.example.borrow.service;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.entity.Category;
import com.example.borrow.entity.Material;
import com.example.borrow.entity.OperationLog;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.CategoryRepository;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.repository.OperationLogRepository;
import com.example.borrow.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MaterialService {

    /** 未完结的记录状态：处于这些状态时，物资不允许删除，总量也不允许调减到不够 */
    private static final List<String> ACTIVE_STATUSES =
            Arrays.asList("PENDING", "BORROWED", "OVERDUE", "PENDING_EXTEND");

    /** 设备状态取值：AVAILABLE-可用，MAINTENANCE-维修中，SCRAPPED-已报废 */
    private static final List<String> DEVICE_STATUSES =
            Arrays.asList("AVAILABLE", "MAINTENANCE", "SCRAPPED");

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private StockLogService stockLogService;

    @Autowired
    private OperationLogRepository operationLogRepository;

    /**
     * 列表 / 搜索。
     * 注意：前端未填写时会传空字符串而非 null，而 JPQL 里用的是 ":category IS NULL" 判断，
     * 空串不是 null，会退化成 "m.category = ''" 从而永远匹配不到任何记录
     * （表现为：只输名称不选类别 → 永远查不到结果）。所以这里统一把空白串归一化为 null。
     */
    public List<Material> list(String keyword, String category) {
        keyword = trimToNull(keyword);
        category = trimToNull(category);
        if (keyword == null && category == null) {
            return materialRepository.findAll();
        }
        return materialRepository.search(keyword, category);
    }

    /** 空白串归一化为 null */
    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public Material getById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物资不存在，ID: " + id));
    }

    @Transactional
    public Material save(Material material) {
        if (material.getId() == null) {
            // 新增：自动生成编号
            if (material.getCode() == null || material.getCode().isEmpty()) {
                material.setCode("M" + System.currentTimeMillis());
            }
            if (material.getAvailableQuantity() == null) {
                material.setAvailableQuantity(material.getTotalQuantity());
            }
            if (material.getWarningThreshold() == null) {
                material.setWarningThreshold(Math.max(2, material.getTotalQuantity() / 5));
            }
            // 设备状态维护：新建物资默认「可用」
            if (material.getDeviceStatus() == null || material.getDeviceStatus().isEmpty()) {
                material.setDeviceStatus("AVAILABLE");
            }
            // 新增业务字段的默认值（未填按“不限制、不收费、非危化品”处理）
            if (material.getBorrowable() == null) material.setBorrowable(true);
            if (material.getNeedsTraining() == null) material.setNeedsTraining(false);
            if (material.getApprovalRequired() == null) material.setApprovalRequired(false);
            if (material.getHourlyPrice() == null) material.setHourlyPrice(0.0);
            if (material.getUnitPrice() == null) material.setUnitPrice(0.0);
            if (material.getHazardous() == null) material.setHazardous(false);
            Material saved = materialRepository.save(material);
            // 库存流水：期初入库
            stockLogService.record(saved, "IN", saved.getAvailableQuantity(), "MANUAL", saved.getId(),
                    "新增物资建档，期初入库");
            return saved;
        }
        return materialRepository.save(material);
    }

    @Transactional
    public Material update(Long id, Material updated) {
        Material material = materialRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("物资不存在，ID: " + id));
        material.setName(updated.getName());
        material.setCategory(updated.getCategory());
        material.setSpecification(updated.getSpecification());
        material.setUnit(updated.getUnit());

        if (updated.getTotalQuantity() == null) {
            throw new RuntimeException("总数量不能为空");
        }
        // 调整总量时同步平移可借数量，并校验不会出现负库存
        int delta = updated.getTotalQuantity() - material.getTotalQuantity();
        int newAvailable = material.getAvailableQuantity() + delta;
        if (newAvailable < 0) {
            throw new RuntimeException("调整后可用数量会变成负数（当前可借 "
                    + material.getAvailableQuantity() + "，本次调减 " + (-delta)
                    + "），请先等未归还的物资归还后再调减总量");
        }
        material.setTotalQuantity(updated.getTotalQuantity());
        material.setAvailableQuantity(newAvailable);

        material.setLocation(updated.getLocation());
        material.setRemark(updated.getRemark());

        // 设备状态维护：AVAILABLE-可用 / MAINTENANCE-维修中 / SCRAPPED-已报废
        if (updated.getDeviceStatus() != null && !updated.getDeviceStatus().isEmpty()) {
            if (!DEVICE_STATUSES.contains(updated.getDeviceStatus())) {
                throw new RuntimeException("无效的设备状态：" + updated.getDeviceStatus());
            }
            material.setDeviceStatus(updated.getDeviceStatus());
        }

        if (updated.getWarningThreshold() != null) {
            material.setWarningThreshold(updated.getWarningThreshold());
        }

        // 借用 / 预约策略、计费、危化品与检定信息
        if (updated.getBorrowable() != null) material.setBorrowable(updated.getBorrowable());
        if (updated.getNeedsTraining() != null) material.setNeedsTraining(updated.getNeedsTraining());
        if (updated.getApprovalRequired() != null) material.setApprovalRequired(updated.getApprovalRequired());
        if (updated.getHourlyPrice() != null) material.setHourlyPrice(updated.getHourlyPrice());
        if (updated.getUnitPrice() != null) material.setUnitPrice(updated.getUnitPrice());
        if (updated.getHazardous() != null) material.setHazardous(updated.getHazardous());
        material.setCasNo(updated.getCasNo());
        material.setValidUntil(updated.getValidUntil());
        material.setCabinetNo(updated.getCabinetNo());
        material.setNextMaintenanceDate(updated.getNextMaintenanceDate());

        Material saved = materialRepository.save(material);
        // 库存流水：手工调整数量（若总量发生变化）
        if (delta != 0) {
            stockLogService.record(saved, "ADJUST", delta, "MANUAL", saved.getId(),
                    "编辑物资，数量调整 " + (delta > 0 ? "+" : "") + delta);
        }
        return saved;
    }

    /**
     * 物资状态维护（教师 + 超管）
     *
     * 与 update(...) 的区别：这里只改 deviceStatus 一个字段，**完全不碰任何数量**。
     * 单独开这个方法的理由：基础数据维护（改总量/规格/删除）应限超管，
     * 而「损坏转维修后，修好了改回可用」是日常操作，教师必须能做——
     * 两者权限面不同，混在同一个接口里只能一刀切，于是形成
     * 「物资转维修后教师改不回来」的死循环。
     */
    @Transactional
    public Material updateStatus(Long id, String deviceStatus) {
        if (deviceStatus == null || deviceStatus.isEmpty()) {
            throw new RuntimeException("物资状态不能为空");
        }
        if (!DEVICE_STATUSES.contains(deviceStatus)) {
            throw new RuntimeException("无效的物资状态：" + deviceStatus
                    + "（可选：" + String.join(" / ", DEVICE_STATUSES) + "）");
        }
        Material material = getById(id);
        String before = material.getDeviceStatus();
        if (deviceStatus.equals(before)) {
            return material;   // 状态没变，不重复记日志
        }

        material.setDeviceStatus(deviceStatus);
        Material saved = materialRepository.save(material);

        // 状态变更写入操作日志：谁、何时、把哪台设备从什么状态改成了什么状态
        try {
            OperationLog log = new OperationLog();
            log.setUsername(UserContext.getUsername());
            log.setRealName(UserContext.getRealName());
            log.setAction("MATERIAL_STATUS");
            log.setDetail("物资「" + saved.getName() + "」状态："
                    + statusLabel(before) + " → " + statusLabel(deviceStatus));
            log.setTargetType("MATERIAL");
            log.setTargetId(saved.getId());
            operationLogRepository.save(log);
        } catch (Exception ignored) {}

        return saved;
    }

    private String statusLabel(String s) {
        if (s == null || s.isEmpty()) return "未设置";
        switch (s) {
            case "AVAILABLE": return "可用";
            case "MAINTENANCE": return "维修中";
            case "SCRAPPED": return "已报废";
            default: return s;
        }
    }

    @Transactional
    public void delete(Long id) {
        Material material = getById(id);
        // 只有「未完结」的记录才阻止删除；已归还/已领用/已拒绝的历史记录不影响删除
        List<BorrowRecord> active = borrowRecordRepository
                .findByMaterialIdAndStatusIn(id, ACTIVE_STATUSES);
        if (!active.isEmpty()) {
            throw new RuntimeException("该物资还有 " + active.size()
                    + " 条未完结的借用/领用记录（待审批/借用中/逾期/延期审批中），无法删除");
        }
        materialRepository.deleteById(id);
    }

    /** 库存预警：可借数量低于预警阈值的物资 */
    public List<Material> lowStock() {
        List<Material> all = materialRepository.findAll();
        List<Material> result = new ArrayList<>();
        for (Material m : all) {
            int threshold = m.getWarningThreshold() != null ? m.getWarningThreshold() : 3;
            if (m.getAvailableQuantity() <= threshold) {
                result.add(m);
            }
        }
        // 按可借数量升序
        result.sort(Comparator.comparingInt(Material::getAvailableQuantity));
        return result;
    }

    /**
     * 库存盘点：按实盘数量批量校正账面数量，差异写入库存流水（ADJUST）。
     *
     * 对应论文中「库存流水的变动类型：入库、借出、消耗、归还与盘点调整」里的盘点调整，
     * 也是期末「台账与实物对不上」问题的处理入口：盘盈补记、盘亏核减，每一笔差异都有流水可查。
     *
     * @param items 每项含 materialId 与 actualQuantity（实盘数量）
     */
    @Transactional
    public Map<String, Object> stockCheck(List<Map<String, Object>> items, String remark) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("盘点明细不能为空");
        }
        int checked = 0, changed = 0, increase = 0, decrease = 0;
        StringBuilder detail = new StringBuilder();

        for (Map<String, Object> item : items) {
            if (item.get("materialId") == null || item.get("actualQuantity") == null) continue;
            Long id = Long.parseLong(String.valueOf(item.get("materialId")));
            int actual;
            try {
                actual = (int) Math.round(Double.parseDouble(String.valueOf(item.get("actualQuantity"))));
            } catch (Exception e) {
                throw new RuntimeException("实盘数量必须是数字");
            }
            if (actual < 0) throw new RuntimeException("实盘数量不能为负数");

            Material material = materialRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new RuntimeException("物资不存在，ID: " + id));
            checked++;
            int book = material.getTotalQuantity() != null ? material.getTotalQuantity() : 0;
            int delta = actual - book;
            if (delta == 0) continue;

            int newAvailable = (material.getAvailableQuantity() != null ? material.getAvailableQuantity() : 0) + delta;
            if (newAvailable < 0) {
                throw new RuntimeException("物资「" + material.getName() + "」盘亏 " + (-delta)
                        + " 件，但当前可借数量只有 " + material.getAvailableQuantity()
                        + "（部分已被借出），请先归还相关物资再盘点");
            }
            material.setTotalQuantity(actual);
            material.setAvailableQuantity(newAvailable);
            Material saved = materialRepository.save(material);

            stockLogService.record(saved, "ADJUST", delta, "STOCK_CHECK", saved.getId(),
                    "盘点调整：账面 " + book + " → 实盘 " + actual + "（" + (delta > 0 ? "盘盈 +" : "盘亏 ") + delta + "）"
                            + (remark != null && !remark.isEmpty() ? "，" + remark : ""));

            changed++;
            if (delta > 0) increase += delta; else decrease += -delta;
            if (detail.length() > 0) detail.append("；");
            detail.append(material.getName()).append(delta > 0 ? "+" : "").append(delta);
        }

        try {
            OperationLog log = new OperationLog();
            log.setUsername(UserContext.getUsername());
            log.setRealName(UserContext.getRealName());
            log.setAction("STOCK_CHECK");
            log.setDetail("库存盘点：核对 " + checked + " 项，调整 " + changed + " 项（盘盈 " + increase
                    + "，盘亏 " + decrease + "）" + (detail.length() > 0 ? "｜" + detail : ""));
            log.setTargetType("MATERIAL");
            operationLogRepository.save(log);
        } catch (Exception ignored) {}

        Map<String, Object> result = new HashMap<>();
        result.put("checked", checked);
        result.put("changed", changed);
        result.put("increase", increase);
        result.put("decrease", decrease);
        return result;
    }

    public Map<String, Object> stats() {
        Map<String, Object> stats = new HashMap<>();
        List<Material> all = materialRepository.findAll();
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderAsc();

        stats.put("totalMaterials", all.size());

        int borrowedCount = 0;
        int lowStockCount = 0;
        for (Material m : all) {
            if (m.getAvailableQuantity() < m.getTotalQuantity()) borrowedCount++;
            int threshold = m.getWarningThreshold() != null ? m.getWarningThreshold() : 3;
            if (m.getAvailableQuantity() <= threshold) lowStockCount++;
        }
        stats.put("borrowedCount", borrowedCount);
        stats.put("lowStockCount", lowStockCount);

        // 动态分类统计
        Map<String, Object> catInfo = new LinkedHashMap<>();
        for (Category cat : categories) {
            long count = all.stream().filter(m -> cat.getCode().equals(m.getCategory())).count();
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("code", cat.getCode());
            info.put("name", cat.getName());
            info.put("count", count);
            catInfo.put(cat.getCode(), info);
        }
        stats.put("categories", catInfo);

        return stats;
    }
}

package com.example.borrow.service;

import com.example.borrow.entity.*;
import com.example.borrow.repository.*;
import com.example.borrow.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 采购入库服务：创建入库单 → 物资库存增加（已存在则补货，不存在则新建）→ 记库存流水
 */
@Service
public class StockInService {

    /** 单据状态：正常 / 已撤销（撤销留痕，单据不物理删除） */
    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_REVOKED = "REVOKED";

    @Autowired private StockInRepository stockInRepository;
    @Autowired private StockInItemRepository stockInItemRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private BorrowRecordRepository borrowRecordRepository;
    @Autowired private StockLogRepository stockLogRepository;
    @Autowired private StockLogService stockLogService;
    @Autowired private OperationLogRepository operationLogRepository;

    /** 入库单列表（分页） */
    public Page<StockIn> list(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createTime"));
        if (keyword != null && !keyword.isEmpty()) {
            return stockInRepository.search(keyword, pageable);
        }
        return stockInRepository.findAll(pageable);
    }

    /** 入库单详情（含明细） */
    public Map<String, Object> detail(Long id) {
        StockIn order = stockInRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("入库单不存在，ID: " + id));
        List<StockInItem> items = stockInItemRepository.findByStockInId(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order);
        result.put("items", items);
        return result;
    }

    /**
     * 创建入库单并入库
     *
     * @param supplier 供应商
     * @param remark   备注
     * @param items    明细（materialId 为空表示新建物资）
     */
    @Transactional
    public StockIn create(String supplier, String remark, List<StockInItem> items) {
        validateItems(items);

        // 1. 保存主单
        StockIn order = new StockIn();
        order.setInNo("IN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(1000)));
        order.setSupplier(supplier);
        order.setOperator(currentOperator());
        order.setRemark(remark);
        order.setTotalQuantity(totalQuantity(items));
        order.setTotalAmount(totalAmount(items));
        order = stockInRepository.save(order);

        // 2. 逐条明细入库（同时写库存流水与明细行）
        applyItems(order, items);

        // 3. 操作日志
        logOperation("STOCK_IN",
                "创建入库单「" + order.getInNo() + "」，供应商：" + (supplier != null ? supplier : "-")
                        + "，共 " + items.size() + " 种物资 ×" + order.getTotalQuantity() + " 件",
                "STOCK_IN", order.getId());

        return order;
    }

    /**
     * 编辑入库单：在同一事务内先整体回退旧明细（数量、明细行、库存流水），
     * 再按新明细重新入库，保证「账实相符」且不会出现改一半的中间态。
     *
     * 若某物资的库存已被借出/领用导致回退后为负，直接拒绝并提示，不做静默截断。
     */
    @Transactional
    public StockIn update(Long id, String supplier, String remark, List<StockInItem> items) {
        StockIn order = stockInRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("入库单不存在，ID: " + id));
        validateItems(items);

        if (STATUS_REVOKED.equals(order.getStatus())) {
            throw new RuntimeException("入库单「" + order.getInNo() + "」已撤销，不能再编辑");
        }

        List<Long> oldMaterialIds = revertItems(order, "编辑入库单", true);

        order.setSupplier(supplier);
        order.setRemark(remark);
        order.setStatus(STATUS_NORMAL);
        order.setTotalQuantity(totalQuantity(items));
        order.setTotalAmount(totalAmount(items));
        order = stockInRepository.save(order);

        applyItems(order, items);

        // 新明细里没用到的旧物资：若是本单建档且已清零，顺手清理，避免列表里留下一堆 0 库存
        Set<Long> reused = new HashSet<>();
        for (StockInItem item : items) {
            if (item.getMaterialId() != null) reused.add(item.getMaterialId());
        }
        for (Long materialId : oldMaterialIds) {
            if (!reused.contains(materialId)) tryDeleteOrphanMaterial(materialId);
        }

        logOperation("STOCK_IN_EDIT",
                "修改入库单「" + order.getInNo() + "」，供应商：" + (supplier != null ? supplier : "-")
                        + "，共 " + items.size() + " 种物资 ×" + order.getTotalQuantity() + " 件",
                "STOCK_IN", order.getId());

        return order;
    }

    /**
     * 撤销入库单：回退库存与库存流水，单据本身保留并标记为「已撤销」（留痕可追溯）。
     *
     * 采用留痕而非物理删除的原因：入库是动了账的动作，答辩与审计都更认可
     * 「记录还在、状态可查、谁撤的看得见」，而不是悄悄消失一张单。
     * 明细行同样保留（物资快照字段齐全），因此撤销后仍能看清这单原本入了什么。
     *
     * 若这批库存已被借出/领用，回退会出现负库存，此时拒绝撤销。
     */
    @Transactional
    public void revoke(Long id) {
        StockIn order = stockInRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("入库单不存在，ID: " + id));
        if (STATUS_REVOKED.equals(order.getStatus())) {
            throw new RuntimeException("入库单「" + order.getInNo() + "」已经撤销过了，无需重复操作");
        }

        List<Long> materialIds = revertItems(order, "撤销入库单", false);

        String operator = currentOperator();
        order.setStatus(STATUS_REVOKED);
        order.setRevokeOperator(operator);
        order.setRevokeTime(LocalDateTime.now());
        stockInRepository.save(order);

        // 本单自动建档、且撤销后清零、且从未被借用的物资：一并清理，避免物资列表残留 0 库存空记录
        for (Long materialId : materialIds) {
            tryDeleteOrphanMaterial(materialId);
        }

        logOperation("STOCK_IN_REVOKE",
                "撤销入库单「" + order.getInNo() + "」（供应商："
                        + (order.getSupplier() != null ? order.getSupplier() : "-")
                        + "），已回退库存 " + (order.getTotalQuantity() != null ? order.getTotalQuantity() : 0) + " 件",
                "STOCK_IN", id);
    }

    /** 明细校验：数量为正、物资名称非空 */
    private void validateItems(List<StockInItem> items) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("入库单至少需要一行明细");
        }
        for (StockInItem item : items) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException("明细「" + item.getMaterialName() + "」入库数量必须大于0");
            }
            if (item.getMaterialName() == null || item.getMaterialName().trim().isEmpty()) {
                throw new RuntimeException("明细物资名称不能为空");
            }
        }
    }

    private int totalQuantity(List<StockInItem> items) {
        return items.stream().mapToInt(StockInItem::getQuantity).sum();
    }

    private double totalAmount(List<StockInItem> items) {
        return items.stream()
                .mapToDouble(i -> (i.getUnitPrice() != null ? i.getUnitPrice() : 0) * i.getQuantity()).sum();
    }

    /**
     * 按明细入库：已有物资补货、新物资建档，并逐条写库存流水与明细行。
     * 调用方需保证 order 已落库（明细要挂 stockInId）。
     */
    private void applyItems(StockIn order, List<StockInItem> items) {
        int index = 0;
        for (StockInItem item : items) {
            Material material;
            if (item.getMaterialId() != null) {
                // 已有物资：补货
                material = materialRepository.findByIdForUpdate(item.getMaterialId())
                        .orElseThrow(() -> new RuntimeException("物资不存在，ID: " + item.getMaterialId()));
                material.setTotalQuantity(material.getTotalQuantity() + item.getQuantity());
                material.setAvailableQuantity(material.getAvailableQuantity() + item.getQuantity());
                material = materialRepository.save(material);
            } else {
                // 新物资：建档并入库
                material = new Material();
                material.setCode("M" + System.currentTimeMillis() + String.format("%02d", index));
                material.setName(item.getMaterialName());
                material.setCategory(item.getCategory() != null ? item.getCategory() : "CONSUMABLE");
                material.setSpecification(item.getSpecification());
                material.setUnit(item.getUnit());
                material.setTotalQuantity(item.getQuantity());
                material.setAvailableQuantity(item.getQuantity());
                material.setWarningThreshold(Math.max(2, item.getQuantity() / 5));
                material.setRemark("来源入库单：" + order.getInNo());
                material = materialRepository.save(material);
            }

            // 库存流水：入库
            stockLogService.record(material, "IN", item.getQuantity(), "STOCK_IN", order.getId(),
                    "采购入库（" + order.getInNo() + "）" + (item.getUnitPrice() != null && item.getUnitPrice() > 0
                            ? "，单价¥" + item.getUnitPrice() : ""));

            // 保存明细
            StockInItem savedItem = new StockInItem();
            savedItem.setStockInId(order.getId());
            savedItem.setMaterialId(material.getId());
            savedItem.setMaterialName(item.getMaterialName());
            savedItem.setCategory(material.getCategory());
            savedItem.setSpecification(item.getSpecification());
            savedItem.setUnit(item.getUnit());
            savedItem.setQuantity(item.getQuantity());
            savedItem.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice() : 0.0);
            stockInItemRepository.save(savedItem);
            index++;
        }
    }

    /**
     * 回退一张入库单的全部影响：库存数量、明细行、该单产生的库存流水。
     *
     * 冲突处理：若某物资的可借数量已小于本单入库量（说明其间已被借出/领用），
     * 回退会出现负库存，此时抛出异常交由事务回滚，不做静默截断——
     * 宁可让操作失败并说明原因，也不能把账面数字改乱。
     *
     * @param deleteItems 是否删除该单的明细行：
     *                    编辑要删（随后按新明细重建）；撤销留痕不删（明细作为历史保留）
     * @return 本次回退涉及到的物资 ID 列表
     */
    private List<Long> revertItems(StockIn order, String action, boolean deleteItems) {
        List<StockInItem> items = stockInItemRepository.findByStockInId(order.getId());

        // 先整体校验，避免校验到一半才失败（虽然有事务兜底，但报错信息更清晰）
        for (StockInItem item : items) {
            if (item.getMaterialId() == null) continue;
            Material material = materialRepository.findByIdForUpdate(item.getMaterialId()).orElse(null);
            if (material == null) continue;
            if (material.getAvailableQuantity() < item.getQuantity()) {
                throw new RuntimeException("无法" + action + "：物资「" + material.getName() + "」当前可借数量仅 "
                        + material.getAvailableQuantity() + "，少于本单入库的 " + item.getQuantity()
                        + " 件（部分已被借出或领用）。请先归还相关物资，或改为在物资管理中调整数量。");
            }
        }

        // 删除该单产生的库存流水（入库记录本身消失，操作痕迹记在操作日志里）
        stockLogRepository.deleteByRefTypeAndRefId("STOCK_IN", order.getId());

        List<Long> materialIds = new ArrayList<>();
        for (StockInItem item : items) {
            if (item.getMaterialId() == null) continue;
            Material material = materialRepository.findByIdForUpdate(item.getMaterialId()).orElse(null);
            if (material == null) continue;
            material.setTotalQuantity(material.getTotalQuantity() - item.getQuantity());
            material.setAvailableQuantity(material.getAvailableQuantity() - item.getQuantity());
            materialRepository.save(material);
            materialIds.add(material.getId());
        }

        if (deleteItems) {
            stockInItemRepository.deleteByStockInId(order.getId());
        }
        return materialIds;
    }

    /**
     * 清理「因入库单自动建档、回退后库存清零、且从未被借用过」的物资，
     * 避免撤销/编辑后物资列表里残留 0 库存的空记录。
     * 手动改过备注、或已有借用记录的物资一律保留。
     */
    private void tryDeleteOrphanMaterial(Long materialId) {
        if (materialId == null) return;
        Material material = materialRepository.findById(materialId).orElse(null);
        if (material == null) return;
        boolean autoCreated = material.getRemark() != null && material.getRemark().startsWith("来源入库单");
        int total = material.getTotalQuantity() != null ? material.getTotalQuantity() : 0;
        int available = material.getAvailableQuantity() != null ? material.getAvailableQuantity() : 0;
        if (autoCreated && total <= 0 && available <= 0 && !borrowRecordRepository.existsByMaterialId(materialId)) {
            materialRepository.deleteById(materialId);
        }
    }

    /** 写操作日志：谁、何时、做了什么（失败不影响主流程） */
    private void logOperation(String action, String detail, String targetType, Long targetId) {
        try {
            OperationLog log = new OperationLog();
            log.setUsername(UserContext.getUsername() != null ? UserContext.getUsername() : currentOperator());
            log.setRealName(UserContext.getRealName() != null ? UserContext.getRealName() : currentOperator());
            log.setAction(action);
            log.setDetail(detail);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            operationLogRepository.save(log);
        } catch (Exception ignored) {
        }
    }

    private String currentOperator() {
        // 与库存流水口径一致：优先取真实姓名，未登录（系统初始化）时记为「系统」
        String realName = UserContext.getRealName();
        return realName != null ? realName : "系统";
    }
}

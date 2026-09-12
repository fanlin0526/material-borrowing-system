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

    @Autowired private StockInRepository stockInRepository;
    @Autowired private StockInItemRepository stockInItemRepository;
    @Autowired private MaterialRepository materialRepository;
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

        // 1. 保存主单
        StockIn order = new StockIn();
        order.setInNo("IN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(1000)));
        order.setSupplier(supplier);
        order.setOperator(currentOperator());
        order.setRemark(remark);
        int totalQty = items.stream().mapToInt(StockInItem::getQuantity).sum();
        double totalAmt = items.stream()
                .mapToDouble(i -> (i.getUnitPrice() != null ? i.getUnitPrice() : 0) * i.getQuantity()).sum();
        order.setTotalQuantity(totalQty);
        order.setTotalAmount(totalAmt);
        order = stockInRepository.save(order);

        // 2. 逐条明细入库
        List<StockInItem> savedItems = new ArrayList<>();
        for (StockInItem item : items) {
            Material material;
            if (item.getMaterialId() != null) {
                // 已有物资：补货
                material = materialRepository.findById(item.getMaterialId())
                        .orElseThrow(() -> new RuntimeException("物资不存在，ID: " + item.getMaterialId()));
                material.setTotalQuantity(material.getTotalQuantity() + item.getQuantity());
                material.setAvailableQuantity(material.getAvailableQuantity() + item.getQuantity());
                material = materialRepository.save(material);
            } else {
                // 新物资：建档并入库
                material = new Material();
                material.setCode("M" + System.currentTimeMillis() + String.format("%02d", savedItems.size()));
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
            savedItems.add(stockInItemRepository.save(savedItem));
        }

        // 3. 操作日志
        try {
            OperationLog log = new OperationLog();
            log.setUsername(order.getOperator());
            log.setRealName(order.getOperator());
            log.setAction("STOCK_IN");
            log.setDetail("创建入库单「" + order.getInNo() + "」，供应商：" + (supplier != null ? supplier : "-")
                    + "，共 " + savedItems.size() + " 种物资 ×" + totalQty + " 件");
            log.setTargetType("STOCK_IN");
            log.setTargetId(order.getId());
            operationLogRepository.save(log);
        } catch (Exception ignored) {}

        return order;
    }

    private String currentOperator() {
        String username = UserContext.getUsername();
        return username != null ? username : "系统";
    }
}

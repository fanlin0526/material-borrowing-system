package com.example.borrow.controller;

import com.example.borrow.entity.Material;
import com.example.borrow.security.RoleGuard;
import com.example.borrow.service.MaterialService;
import com.example.borrow.service.StockLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private StockLogService stockLogService;

    /** 列表 / 搜索 */
    @GetMapping
    public Result<?> list(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String category) {
        return Result.ok(materialService.list(keyword, category));
    }

    /** 详情 */
    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Long id) {
        return Result.ok(materialService.getById(id));
    }

    /** 新增（基础数据维护：仅超级管理员） */
    @PostMapping
    public Result<?> add(@Valid @RequestBody Material material) {
        RoleGuard.requireSuperAdmin();
        return Result.ok(materialService.save(material));
    }

    /** 修改（基础数据维护：仅超级管理员） */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody Material material) {
        RoleGuard.requireSuperAdmin();
        return Result.ok(materialService.update(id, material));
    }

    /** 删除（基础数据维护：仅超级管理员） */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        RoleGuard.requireSuperAdmin();
        materialService.delete(id);
        return Result.ok();
    }

    /**
     * 物资状态维护（教师 + 超管）
     *
     * 只接受 deviceStatus 一个字段，不碰任何数量——所以放宽到教师
     * 不会带来基础数据被篡改的风险。用于「损坏转维修后改回可用」等日常维护。
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        RoleGuard.requireStaff();
        return Result.ok(materialService.updateStatus(id, body.get("deviceStatus")));
    }

    /** 统计 */
    @GetMapping("/stats")
    public Result<?> stats() {
        return Result.ok(materialService.stats());
    }

    /** 库存预警列表 */
    @GetMapping("/low-stock")
    public Result<?> lowStock() {
        return Result.ok(materialService.lowStock());
    }

    /**
     * 库存盘点（教师 + 超管）：按实盘数量批量校正账面数量，差异写入库存流水。
     * 请求体：{ "remark": "期末盘点", "items": [ { "materialId": 1, "actualQuantity": 8 } ] }
     */
    @PostMapping("/stock-check")
    public Result<?> stockCheck(@RequestBody Map<String, Object> body) {
        RoleGuard.requireStaff();
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> items =
                (java.util.List<Map<String, Object>>) body.get("items");
        return Result.ok(materialService.stockCheck(items, (String) body.get("remark")));
    }

    /** 库存流水（按物资查，分页） */
    @GetMapping("/{id}/logs")
    public Result<?> logs(@PathVariable Long id,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(stockLogService.listByMaterial(id, page, size));
    }
}

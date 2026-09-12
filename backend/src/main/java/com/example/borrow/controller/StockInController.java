package com.example.borrow.controller;

import com.example.borrow.entity.StockIn;
import com.example.borrow.entity.StockInItem;
import com.example.borrow.service.StockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-in")
public class StockInController {

    @Autowired
    private StockInService stockInService;

    /** 入库单列表（分页） */
    @GetMapping
    public Result<?> list(@RequestParam(required = false) String keyword,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        Page<StockIn> result = stockInService.list(keyword, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("content", result.getContent());
        data.put("totalElements", result.getTotalElements());
        data.put("totalPages", result.getTotalPages());
        data.put("page", page);
        data.put("size", size);
        return Result.ok(data);
    }

    /** 入库单详情（含明细） */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(stockInService.detail(id));
    }

    /** 创建入库单（提交后自动增加库存并记流水） */
    @PostMapping
    public Result<?> create(@RequestBody CreateRequest request) {
        StockIn order = stockInService.create(request.getSupplier(), request.getRemark(), request.getItems());
        return Result.ok(order);
    }

    static class CreateRequest {
        private String supplier;
        private String remark;
        private List<StockInItem> items;
        public String getSupplier() { return supplier; }
        public void setSupplier(String supplier) { this.supplier = supplier; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public List<StockInItem> getItems() { return items; }
        public void setItems(List<StockInItem> items) { this.items = items; }
    }
}

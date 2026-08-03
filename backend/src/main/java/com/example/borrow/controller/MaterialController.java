package com.example.borrow.controller;

import com.example.borrow.entity.Material;
import com.example.borrow.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

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

    /** 新增 */
    @PostMapping
    public Result<?> add(@Valid @RequestBody Material material) {
        return Result.ok(materialService.save(material));
    }

    /** 修改 */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody Material material) {
        return Result.ok(materialService.update(id, material));
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        materialService.delete(id);
        return Result.ok();
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
}

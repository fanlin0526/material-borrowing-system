package com.example.borrow.controller;

import com.example.borrow.entity.Category;
import com.example.borrow.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /** 列表 */
    @GetMapping
    public Result<?> list() {
        return Result.ok(categoryService.list());
    }

    /** 详情 */
    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Long id) {
        return Result.ok(categoryService.getById(id));
    }

    /** 新增 */
    @PostMapping
    public Result<?> add(@Valid @RequestBody Category category) {
        return Result.ok(categoryService.save(category));
    }

    /** 修改 */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody Category category) {
        return Result.ok(categoryService.update(id, category));
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}

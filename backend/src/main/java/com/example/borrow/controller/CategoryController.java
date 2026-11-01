package com.example.borrow.controller;

import com.example.borrow.entity.Category;
import com.example.borrow.security.RoleGuard;
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

    /** 新增（仅超级管理员：分类是物资归类的基础口径） */
    @PostMapping
    public Result<?> add(@Valid @RequestBody Category category) {
        RoleGuard.requireSuperAdmin();
        return Result.ok(categoryService.save(category));
    }

    /** 修改（仅超级管理员） */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody Category category) {
        RoleGuard.requireSuperAdmin();
        return Result.ok(categoryService.update(id, category));
    }

    /** 删除（仅超级管理员） */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        RoleGuard.requireSuperAdmin();
        categoryService.delete(id);
        return Result.ok();
    }
}

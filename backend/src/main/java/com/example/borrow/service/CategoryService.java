package com.example.borrow.service;

import com.example.borrow.entity.Category;
import com.example.borrow.entity.Material;
import com.example.borrow.repository.CategoryRepository;
import com.example.borrow.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MaterialRepository materialRepository;

    public List<Category> list() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在，ID: " + id));
    }

    @Transactional
    public Category save(Category category) {
        if (categoryRepository.existsByCode(category.getCode())) {
            throw new RuntimeException("分类编码已存在: " + category.getCode());
        }
        if (category.getSortOrder() == null) category.setSortOrder(0);
        if (category.getReturnable() == null) category.setReturnable(false);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, Category updated) {
        Category cat = getById(id);
        // 如果 code 变更，检查唯一性
        if (!cat.getCode().equals(updated.getCode())) {
            if (categoryRepository.existsByCode(updated.getCode())) {
                throw new RuntimeException("分类编码已存在: " + updated.getCode());
            }
            // 同步更新所有该分类下的物资
            List<Material> materials = materialRepository.findByCategory(cat.getCode());
            for (Material m : materials) {
                m.setCategory(updated.getCode());
            }
            materialRepository.saveAll(materials);
        }
        cat.setCode(updated.getCode());
        cat.setName(updated.getName());
        cat.setDescription(updated.getDescription());
        cat.setSortOrder(updated.getSortOrder());
        cat.setReturnable(updated.getReturnable() != null ? updated.getReturnable() : false);
        return categoryRepository.save(cat);
    }

    @Transactional
    public void delete(Long id) {
        Category cat = getById(id);
        // 检查是否有物资使用该分类
        List<Material> materials = materialRepository.findByCategory(cat.getCode());
        if (!materials.isEmpty()) {
            throw new RuntimeException("该分类下还有 " + materials.size() + " 个物资，无法删除。请先清理或转移这些物资。");
        }
        categoryRepository.deleteById(id);
    }

    /** 根据分类编码判断是否可归还 */
    public boolean isReturnable(String code) {
        return categoryRepository.findByCode(code)
                .map(Category::getReturnable)
                .orElse(false);
    }
}

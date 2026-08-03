package com.example.borrow.service;

import com.example.borrow.entity.Category;
import com.example.borrow.entity.Material;
import com.example.borrow.repository.CategoryRepository;
import com.example.borrow.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Material> list(String keyword, String category) {
        if ((keyword == null || keyword.isEmpty()) && (category == null || category.isEmpty())) {
            return materialRepository.findAll();
        }
        return materialRepository.search(keyword, category);
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
        }
        return materialRepository.save(material);
    }

    @Transactional
    public Material update(Long id, Material updated) {
        Material material = getById(id);
        material.setName(updated.getName());
        material.setCategory(updated.getCategory());
        material.setSpecification(updated.getSpecification());
        material.setUnit(updated.getUnit());

        int delta = updated.getTotalQuantity() - material.getTotalQuantity();
        material.setTotalQuantity(updated.getTotalQuantity());
        material.setAvailableQuantity(material.getAvailableQuantity() + delta);

        material.setLocation(updated.getLocation());
        material.setRemark(updated.getRemark());
        if (updated.getWarningThreshold() != null) {
            material.setWarningThreshold(updated.getWarningThreshold());
        }
        return materialRepository.save(material);
    }

    @Transactional
    public void delete(Long id) {
        Material material = getById(id);
        if (material.getAvailableQuantity() < material.getTotalQuantity()) {
            throw new RuntimeException("该物资还有借用中的记录，无法删除");
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

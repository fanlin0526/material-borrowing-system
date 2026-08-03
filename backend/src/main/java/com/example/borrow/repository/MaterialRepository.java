package com.example.borrow.repository;

import com.example.borrow.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByCategory(String category);

    @Query("SELECT m FROM Material m WHERE " +
           "(:keyword IS NULL OR m.name LIKE %:keyword% OR m.code LIKE %:keyword%) " +
           "AND (:category IS NULL OR m.category = :category)")
    List<Material> search(String keyword, String category);

    boolean existsByCode(String code);
}

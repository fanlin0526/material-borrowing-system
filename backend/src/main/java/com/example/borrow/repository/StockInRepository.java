package com.example.borrow.repository;

import com.example.borrow.entity.StockIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockInRepository extends JpaRepository<StockIn, Long> {

    @Query("SELECT s FROM StockIn s WHERE " +
           "(:keyword IS NULL OR s.inNo LIKE %:keyword% OR s.supplier LIKE %:keyword% OR s.operator LIKE %:keyword%)")
    Page<StockIn> search(String keyword, Pageable pageable);

    List<StockIn> findAllByOrderByCreateTimeDesc();
}

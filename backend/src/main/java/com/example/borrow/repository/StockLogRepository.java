package com.example.borrow.repository;

import com.example.borrow.entity.StockLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockLogRepository extends JpaRepository<StockLog, Long> {

    List<StockLog> findByMaterialIdOrderByCreateTimeDesc(Long materialId);

    Page<StockLog> findByMaterialIdOrderByCreateTimeDesc(Long materialId, Pageable pageable);
}

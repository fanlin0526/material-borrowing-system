package com.example.borrow.repository;

import com.example.borrow.entity.StockInItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockInItemRepository extends JpaRepository<StockInItem, Long> {

    List<StockInItem> findByStockInId(Long stockInId);
}

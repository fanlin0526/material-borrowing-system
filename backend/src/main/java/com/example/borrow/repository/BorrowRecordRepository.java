package com.example.borrow.repository;

import com.example.borrow.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByStatusOrderByBorrowTimeDesc(String status);

    List<BorrowRecord> findByBorrowerContainingOrderByBorrowTimeDesc(String borrower);

    List<BorrowRecord> findAllByOrderByBorrowTimeDesc();

    @Query("SELECT r FROM BorrowRecord r WHERE " +
           "(:status IS NULL OR r.status = :status) " +
           "AND (:keyword IS NULL OR r.borrower LIKE %:keyword% OR r.materialName LIKE %:keyword%) " +
           "ORDER BY r.borrowTime DESC")
    List<BorrowRecord> search(String status, String keyword);

    List<BorrowRecord> findByMaterialIdAndStatus(Long materialId, String status);
}

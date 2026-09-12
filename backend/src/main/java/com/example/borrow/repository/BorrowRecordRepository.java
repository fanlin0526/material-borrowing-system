package com.example.borrow.repository;

import com.example.borrow.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByStatusOrderByBorrowTimeDesc(String status);

    Page<BorrowRecord> findByStatusOrderByBorrowTimeDesc(String status, Pageable pageable);

    List<BorrowRecord> findByBorrowerContainingOrderByBorrowTimeDesc(String borrower);

    List<BorrowRecord> findAllByOrderByBorrowTimeDesc();

    Page<BorrowRecord> findAllByOrderByBorrowTimeDesc(Pageable pageable);

    @Query("SELECT r FROM BorrowRecord r WHERE " +
           "(:keyword IS NULL OR r.borrower LIKE %:keyword% OR r.materialName LIKE %:keyword%) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "ORDER BY r.borrowTime DESC")
    List<BorrowRecord> search(String status, String keyword);

    @Query("SELECT r FROM BorrowRecord r WHERE " +
           "(:keyword IS NULL OR r.borrower LIKE %:keyword% OR r.materialName LIKE %:keyword%) " +
           "ORDER BY r.borrowTime DESC")
    Page<BorrowRecord> search(String keyword, Pageable pageable);

    List<BorrowRecord> findByMaterialIdAndStatus(Long materialId, String status);
}

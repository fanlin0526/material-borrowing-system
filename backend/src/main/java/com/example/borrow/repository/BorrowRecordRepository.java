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

    List<BorrowRecord> findByBorrowerUsernameOrderByBorrowTimeDesc(String borrowerUsername);

    List<BorrowRecord> findByBorrowerUsernameAndStatusIn(String borrowerUsername, java.util.Collection<String> statuses);

    List<BorrowRecord> findByRequestNoOrderByBorrowTimeAsc(String requestNo);

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

    /**
     * 组合查询：借用人（精确，用于"只看我的记录"）+ 状态 + 关键字（物资名/借用人）三者可同时生效。
     * 学生端此前是"用本人姓名覆盖关键字"，导致按物资名搜索失效 —— 这里拆成两个独立条件即可并存。
     */
    @Query("SELECT r FROM BorrowRecord r WHERE " +
           "(:borrowerName IS NULL OR r.borrower = :borrowerName) " +
           "AND (:borrowerUsername IS NULL OR r.borrowerUsername = :borrowerUsername) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:keyword IS NULL OR r.borrower LIKE %:keyword% OR r.materialName LIKE %:keyword%) " +
           "ORDER BY r.borrowTime DESC")
    Page<BorrowRecord> searchAdvanced(String status, String keyword, String borrowerName,
                                     String borrowerUsername, Pageable pageable);

    List<BorrowRecord> findByMaterialIdAndStatus(Long materialId, String status);

    /** 该物资是否已产生过借用/领用记录（用于撤销入库时判断物资能否一并清理） */
    boolean existsByMaterialId(Long materialId);

    /** 查某物资下处于给定状态的记录（用于判断是否还有未完结的借用/领用） */
    List<BorrowRecord> findByMaterialIdAndStatusIn(Long materialId, java.util.Collection<String> statuses);
}

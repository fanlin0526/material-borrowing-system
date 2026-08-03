package com.example.borrow.repository;

import com.example.borrow.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 检测时间段冲突：两个区间 [start, end] 重叠的充要条件是 NOT (end <= start OR start >= end) */
    @Query("SELECT r FROM Reservation r WHERE r.materialId = :materialId " +
           "AND r.status IN ('APPROVED') " +
           "AND NOT (r.endTime <= :startTime OR r.startTime >= :endTime)")
    List<Reservation> findConflicting(@Param("materialId") Long materialId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /** 按物资和日期范围查预约 */
    @Query("SELECT r FROM Reservation r WHERE r.materialId = :materialId " +
           "AND r.status IN ('APPROVED') " +
           "AND r.startTime >= :from AND r.endTime <= :to " +
           "ORDER BY r.startTime ASC")
    List<Reservation> findByMaterialAndDateRange(@Param("materialId") Long materialId,
                                                   @Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to);

    /** 按状态查询 */
    List<Reservation> findByStatusOrderByCreateTimeDesc(String status);

    /** 按用户名查询 */
    List<Reservation> findByUsernameOrderByCreateTimeDesc(String username);

    /** 查某个物资的所有已通过预约 */
    List<Reservation> findByMaterialIdAndStatus(Long materialId, String status);
}

package com.example.borrow.repository;

import com.example.borrow.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByCategory(String category);

    @Query("SELECT m FROM Material m WHERE " +
           "(:keyword IS NULL OR m.name LIKE %:keyword% OR m.code LIKE %:keyword%) " +
           "AND (:category IS NULL OR m.category = :category)")
    List<Material> search(String keyword, String category);

    boolean existsByCode(String code);

    /**
     * 审批、归还、预约出库等库存变更入口统一使用行锁。
     * 同一物资的库存计算因此串行执行，避免两个并发审批都读到旧库存后覆盖写回。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Material m WHERE m.id = :id")
    Optional<Material> findByIdForUpdate(Long id);

    /**
     * 修复旧版本数据中的空乐观锁版本。
     *
     * 旧库执行 ddl-auto=update 新增 version 列后，历史行会得到 NULL。
     * Hibernate 更新这些行时会对 NULL 做 Long 自增，从而抛出空指针。
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE t_material SET version = 0 WHERE version IS NULL", nativeQuery = true)
    int initNullVersions();
}

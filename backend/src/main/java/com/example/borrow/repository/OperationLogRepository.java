package com.example.borrow.repository;

import com.example.borrow.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    List<OperationLog> findByUsernameOrderByCreateTimeDesc(String username);
    List<OperationLog> findAllByOrderByCreateTimeDesc();
}

package com.example.borrow.repository;

import com.example.borrow.entity.TrainingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {

    List<TrainingRecord> findByMaterialIdOrderByTrainDateDesc(Long materialId);

    List<TrainingRecord> findByUsernameOrderByTrainDateDesc(String username);

    List<TrainingRecord> findByMaterialIdAndUsername(Long materialId, String username);
}

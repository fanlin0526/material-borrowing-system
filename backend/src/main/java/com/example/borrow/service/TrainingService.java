package com.example.borrow.service;

import com.example.borrow.entity.TrainingRecord;
import com.example.borrow.repository.TrainingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 培训 / 上机资质服务。
 *
 * 真实实验室对大型仪器普遍要求「先培训后使用」，本服务是这条规则的唯一判定口径：
 * 物资标记 needsTraining 时，借用与预约都必须先通过这里校验。
 */
@Service
public class TrainingService {

    @Autowired
    private TrainingRecordRepository trainingRecordRepository;

    /** 某人是否具备某物资的有效上机资质（无记录或已过期都不算） */
    public boolean hasValidTraining(Long materialId, String username) {
        if (materialId == null || username == null || username.isEmpty()) return false;
        LocalDate today = LocalDate.now();
        return trainingRecordRepository.findByMaterialIdAndUsername(materialId, username).stream()
                .anyMatch(r -> r.getValidUntil() == null || !r.getValidUntil().isBefore(today));
    }

    public List<TrainingRecord> listByMaterial(Long materialId) {
        return trainingRecordRepository.findByMaterialIdOrderByTrainDateDesc(materialId);
    }

    public List<TrainingRecord> listByUsername(String username) {
        return trainingRecordRepository.findByUsernameOrderByTrainDateDesc(username);
    }

    public List<TrainingRecord> listAll() {
        return trainingRecordRepository.findAll();
    }
}

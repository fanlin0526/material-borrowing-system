package com.example.borrow.service;

import com.example.borrow.entity.Material;
import com.example.borrow.entity.StockLog;
import com.example.borrow.repository.StockLogRepository;
import com.example.borrow.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 库存流水服务：所有库存变动统一走这里记账，保证"账实相符"可追溯
 */
@Service
public class StockLogService {

    @Autowired
    private StockLogRepository stockLogRepository;

    /**
     * 记录一条库存流水（在库存已变更之后调用）
     *
     * @param material     物资（须为已保存的最新状态）
     * @param changeType   变动类型 IN/OUT_BORROW/RETURN/CONSUME/ADJUST
     * @param delta        变动数量（正=入库增加，负=出库减少）
     * @param refType      关联单据类型 BORROW/STOCK_IN/MANUAL
     * @param refId        关联单据ID
     * @param remark       备注
     */
    @Transactional
    public void record(Material material, String changeType, int delta,
                       String refType, Long refId, String remark) {
        if (delta == 0) return;

        StockLog log = new StockLog();
        log.setMaterialId(material.getId());
        log.setMaterialName(material.getName());
        log.setChangeType(changeType);
        log.setChangeQuantity(delta);
        log.setBeforeQuantity(material.getAvailableQuantity() - delta);
        log.setAfterQuantity(material.getAvailableQuantity());
        log.setOperator(currentOperator());
        log.setRefType(refType);
        log.setRefId(refId);
        log.setRemark(remark);
        stockLogRepository.save(log);
    }

    /** 单个物资的流水（按时间倒序） */
    public List<StockLog> listByMaterial(Long materialId) {
        return stockLogRepository.findByMaterialIdOrderByCreateTimeDesc(materialId);
    }

    /** 单个物资的流水（分页） */
    public Page<StockLog> listByMaterial(Long materialId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return stockLogRepository.findByMaterialIdOrderByCreateTimeDesc(materialId, pageable);
    }

    /** 全部流水（分页，按时间倒序） */
    public Page<StockLog> listAll(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createTime"));
        return stockLogRepository.findAll(pageable);
    }

    private String currentOperator() {
        String username = UserContext.getUsername();
        return username != null ? username : "系统";
    }
}

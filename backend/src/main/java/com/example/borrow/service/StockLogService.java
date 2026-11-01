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

import java.time.LocalDateTime;
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

    /**
     * 补录一条历史库存流水：变动前后数量与记账时间均由调用方给定。
     *
     * 与 {@link #record} 的区别在于不读取物资的「当前」可借数量——
     * 批量重建历史台账时物资已处于最终状态，只有调用方按时间顺序重放才能算出正确的中间态，
     * 因此这里要求显式传入 before/after，并允许把记账时间回填为业务发生时间。
     *
     * @param material     物资
     * @param changeType   变动类型 IN/OUT_BORROW/RETURN/CONSUME/ADJUST
     * @param delta        变动数量（正=入库增加，负=出库减少）
     * @param beforeQuantity 变动前的可借数量
     * @param afterQuantity  变动后的可借数量
     * @param refType      关联单据类型 BORROW/STOCK_IN/MANUAL
     * @param refId        关联单据ID
     * @param remark       备注
     * @param createTime   记账时间（业务发生时间；传 null 则取当前时间）
     */
    @Transactional
    public void recordAt(Material material, String changeType, int delta,
                         int beforeQuantity, int afterQuantity,
                         String refType, Long refId, String remark,
                         LocalDateTime createTime) {
        if (delta == 0) return;

        StockLog log = new StockLog();
        log.setMaterialId(material.getId());
        log.setMaterialName(material.getName());
        log.setChangeType(changeType);
        log.setChangeQuantity(delta);
        log.setBeforeQuantity(beforeQuantity);
        log.setAfterQuantity(afterQuantity);
        log.setOperator(currentOperator());
        log.setRefType(refType);
        log.setRefId(refId);
        log.setRemark(remark);
        log.setCreateTime(createTime);
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
        // 优先取登录态的真实姓名（如「范林」而非「fanlin」）；未登录时说明是系统自动动作
        // （DataInit 初始化、定时任务），记为「系统」
        String realName = UserContext.getRealName();
        return realName != null ? realName : "系统";
    }
}

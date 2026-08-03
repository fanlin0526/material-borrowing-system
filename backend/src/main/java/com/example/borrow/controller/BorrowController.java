package com.example.borrow.controller;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    /** 借用申请（学生→PENDING，需教师审批） */
    @PostMapping
    public Result<?> requestBorrow(@RequestBody BorrowRequest request) {
        LocalDateTime expectedReturn = null;
        if (request.getExpectedReturnTime() != null && !request.getExpectedReturnTime().isEmpty()) {
            expectedReturn = LocalDateTime.parse(request.getExpectedReturnTime());
        }
        BorrowRecord record = borrowService.requestBorrow(
                request.getMaterialId(), request.getBorrower(),
                request.getQuantity(), request.getPurpose(), expectedReturn);
        return Result.ok(record);
    }

    /** 批量借用申请 */
    @PostMapping("/batch")
    public Result<?> batchBorrow(@RequestBody List<BatchBorrowRequest> requests) {
        return Result.fail("批量借用需逐个走审批流程，已禁用");
    }

    /** 审批通过 */
    @PutMapping("/approve/{recordId}")
    public Result<?> approve(@PathVariable Long recordId, @RequestBody Map<String, String> body) {
        String approver = body.getOrDefault("approver", "管理员");
        return Result.ok(borrowService.approveBorrow(recordId, approver));
    }

    /** 审批拒绝 */
    @PutMapping("/reject/{recordId}")
    public Result<?> reject(@PathVariable Long recordId, @RequestBody Map<String, String> body) {
        String approver = body.getOrDefault("approver", "管理员");
        String reason = body.get("reason");
        borrowService.rejectBorrow(recordId, approver, reason);
        return Result.ok();
    }

    /** 申请延期 */
    @PutMapping("/extend/{recordId}")
    public Result<?> requestExtension(@PathVariable Long recordId, @RequestBody Map<String, Object> body) {
        String borrower = (String) body.get("borrower");
        int days = body.get("days") != null ? Integer.parseInt(body.get("days").toString()) : 7;
        String reason = (String) body.get("reason");
        borrowService.requestExtension(recordId, borrower, days, reason);
        return Result.ok("延期申请已提交");
    }

    /** 审批延期通过 */
    @PutMapping("/extend/approve/{recordId}")
    public Result<?> approveExtension(@PathVariable Long recordId, @RequestBody Map<String, String> body) {
        String approver = body.getOrDefault("approver", "管理员");
        borrowService.approveExtension(recordId, approver);
        return Result.ok();
    }

    /** 审批延期拒绝 */
    @PutMapping("/extend/reject/{recordId}")
    public Result<?> rejectExtension(@PathVariable Long recordId, @RequestBody Map<String, String> body) {
        String approver = body.getOrDefault("approver", "管理员");
        borrowService.rejectExtension(recordId, approver);
        return Result.ok();
    }

    /** 归还 */
    @PutMapping("/return/{recordId}")
    public Result<?> returnMaterial(@PathVariable Long recordId, @RequestBody ReturnRequest request) {
        Map<String, Object> result = borrowService.returnMaterial(
                recordId, request.getReturnQuantity(), request.getReturnRemark(),
                request.getIsDamaged(), request.getDamageDescription());
        return Result.ok(result);
    }

    /** 记录列表 */
    @GetMapping
    public Result<?> list(@RequestParam(required = false) String status,
                          @RequestParam(required = false) String keyword) {
        return Result.ok(borrowService.list(status, keyword));
    }

    /** 待审批列表（教师用） */
    @GetMapping("/pending")
    public Result<?> pending() {
        return Result.ok(borrowService.pendingApprovals());
    }

    /** 统计 */
    @GetMapping("/stats")
    public Result<?> stats() {
        return Result.ok(borrowService.stats());
    }

    static class BorrowRequest {
        private Long materialId; private String borrower; private Integer quantity;
        private String purpose; private String expectedReturnTime;
        public Long getMaterialId() { return materialId; }
        public void setMaterialId(Long materialId) { this.materialId = materialId; }
        public String getBorrower() { return borrower; }
        public void setBorrower(String borrower) { this.borrower = borrower; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        public String getExpectedReturnTime() { return expectedReturnTime; }
        public void setExpectedReturnTime(String expectedReturnTime) { this.expectedReturnTime = expectedReturnTime; }
    }

    static class ReturnRequest {
        private Integer returnQuantity; private String returnRemark;
        private Boolean isDamaged; private String damageDescription;
        public Integer getReturnQuantity() { return returnQuantity; }
        public void setReturnQuantity(Integer returnQuantity) { this.returnQuantity = returnQuantity; }
        public String getReturnRemark() { return returnRemark; }
        public void setReturnRemark(String returnRemark) { this.returnRemark = returnRemark; }
        public Boolean getIsDamaged() { return isDamaged; }
        public void setIsDamaged(Boolean isDamaged) { this.isDamaged = isDamaged; }
        public String getDamageDescription() { return damageDescription; }
        public void setDamageDescription(String damageDescription) { this.damageDescription = damageDescription; }
    }

    static class BatchBorrowRequest {
        private Long materialId; private String materialName; private String borrower;
        private Integer quantity; private String purpose; private String expectedReturnTime;
        public Long getMaterialId() { return materialId; }
        public void setMaterialId(Long materialId) { this.materialId = materialId; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public String getBorrower() { return borrower; }
        public void setBorrower(String borrower) { this.borrower = borrower; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        public String getExpectedReturnTime() { return expectedReturnTime; }
        public void setExpectedReturnTime(String expectedReturnTime) { this.expectedReturnTime = expectedReturnTime; }
    }
}

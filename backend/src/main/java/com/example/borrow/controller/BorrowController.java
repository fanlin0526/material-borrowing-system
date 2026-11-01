package com.example.borrow.controller;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.security.RoleGuard;
import com.example.borrow.security.UserContext;
import com.example.borrow.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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
            expectedReturn = parseDateTime(request.getExpectedReturnTime());
        }
        // 借用人取自登录态，不取请求体：请求体是客户端可控的，可被伪造成任意人名
        String operator = RoleGuard.currentOperator();
        String username = UserContext.getUsername();
        // 教师 / 超管自助借用免审批：直接借出并落一条记录；学生、「借用需审批」的物资仍是「申请 → 审批」
        boolean selfService = RoleGuard.isStaff() && borrowService.canSelfBorrow(request.getMaterialId());
        BorrowRecord record = selfService
                ? borrowService.borrowByStaff(request.getMaterialId(), operator, username,
                        request.getQuantity(), request.getPurpose(), expectedReturn, request.getSecondBorrower())
                : borrowService.requestBorrow(request.getMaterialId(), operator, username,
                        request.getQuantity(), request.getPurpose(), expectedReturn, request.getSecondBorrower());
        return Result.ok(record);
    }

    /** 一次提交多项物资；普通物资对教师/管理员仍免审批，含高值物资时整单审批 */
    @PostMapping("/batch")
    public Result<?> requestBatch(@RequestBody BatchRequest request) {
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new RuntimeException("请至少选择一项物资");
        }
        LocalDateTime expectedReturn = parseDateTime(request.getExpectedReturnTime());
        return Result.ok(borrowService.requestBatch(RoleGuard.currentOperator(), UserContext.getUsername(),
                request.getPurpose(), expectedReturn, request.getLines(), RoleGuard.isStaff()));
    }

    @PutMapping("/batch/approve/{requestNo}")
    public Result<?> approveBatch(@PathVariable String requestNo) {
        RoleGuard.requireStaff();
        return Result.ok(borrowService.approveBatch(requestNo, RoleGuard.currentOperator()));
    }

    @PutMapping("/batch/reject/{requestNo}")
    public Result<?> rejectBatch(@PathVariable String requestNo,
                                 @RequestBody(required = false) Map<String, String> body) {
        RoleGuard.requireStaff();
        borrowService.rejectBatch(requestNo, RoleGuard.currentOperator(),
                body != null ? body.get("reason") : null);
        return Result.ok();
    }

    @PutMapping("/batch/return-edit/{requestNo}")
    public Result<?> returnBatchForEdit(@PathVariable String requestNo,
                                        @RequestBody(required = false) Map<String, String> body) {
        RoleGuard.requireStaff();
        borrowService.returnBatchForEdit(requestNo, RoleGuard.currentOperator(),
                body != null ? body.get("reason") : null);
        return Result.ok();
    }

    @PutMapping("/batch/cancel/{requestNo}")
    public Result<?> cancelBatch(@PathVariable String requestNo) {
        borrowService.cancelBatch(requestNo, UserContext.getUsername());
        return Result.ok();
    }

    /** 申请人撤回待审批的借用申请（学生误提交、临时取消实验时不必等教师拒绝） */
    @PutMapping("/cancel/{recordId}")
    public Result<?> cancelRequest(@PathVariable Long recordId) {
        borrowService.cancelRequest(recordId, UserContext.getUsername());
        return Result.ok();
    }

    /** 退回修改（教师 / 超管）：申请信息有误时退回，申请人改完可重新提交 */
    @PutMapping("/return-edit/{recordId}")
    public Result<?> returnForEdit(@PathVariable Long recordId,
                                   @RequestBody(required = false) Map<String, String> body) {
        RoleGuard.requireStaff();
        borrowService.returnForEdit(recordId, RoleGuard.currentOperator(),
                body != null ? body.get("reason") : null);
        return Result.ok();
    }

    /** 退回后修改并重新提交（申请人本人） */
    @PutMapping("/resubmit/{recordId}")
    public Result<?> resubmit(@PathVariable Long recordId, @RequestBody Map<String, Object> body) {
        Integer quantity = body.get("quantity") != null
                ? (int) Math.round(Double.parseDouble(String.valueOf(body.get("quantity")))) : null;
        String expected = (String) body.get("expectedReturnTime");
        LocalDateTime expectedReturn = (expected != null && !expected.isEmpty())
                ? parseDateTime(expected) : null;
        return Result.ok(borrowService.resubmit(recordId, UserContext.getUsername(), quantity,
                expectedReturn, (String) body.get("purpose"), (String) body.get("secondBorrower")));
    }

    /** 审批通过 */
    @PutMapping("/approve/{recordId}")
    public Result<?> approve(@PathVariable Long recordId, @RequestBody(required = false) Map<String, String> body) {
        RoleGuard.requireStaff();
        return Result.ok(borrowService.approveBorrow(recordId, RoleGuard.currentOperator()));
    }

    /** 审批拒绝 */
    @PutMapping("/reject/{recordId}")
    public Result<?> reject(@PathVariable Long recordId, @RequestBody(required = false) Map<String, String> body) {
        RoleGuard.requireStaff();
        String reason = body != null ? body.get("reason") : null;
        borrowService.rejectBorrow(recordId, RoleGuard.currentOperator(), reason);
        return Result.ok();
    }

    /** 申请延期 */
    @PutMapping("/extend/{recordId}")
    public Result<?> requestExtension(@PathVariable Long recordId, @RequestBody Map<String, Object> body) {
        requireOwnOrStaff(recordId);
        int days = body.get("days") != null ? Integer.parseInt(body.get("days").toString()) : 7;
        String reason = (String) body.get("reason");
        borrowService.requestExtension(recordId, RoleGuard.currentOperator(), days, reason);
        return Result.ok("延期申请已提交");
    }

    /** 审批延期通过 */
    @PutMapping("/extend/approve/{recordId}")
    public Result<?> approveExtension(@PathVariable Long recordId, @RequestBody(required = false) Map<String, String> body) {
        RoleGuard.requireStaff();
        borrowService.approveExtension(recordId, RoleGuard.currentOperator());
        return Result.ok();
    }

    /** 审批延期拒绝 */
    @PutMapping("/extend/reject/{recordId}")
    public Result<?> rejectExtension(@PathVariable Long recordId, @RequestBody(required = false) Map<String, String> body) {
        RoleGuard.requireStaff();
        borrowService.rejectExtension(recordId, RoleGuard.currentOperator());
        return Result.ok();
    }

    /** 归还 */
    @PutMapping("/return/{recordId}")
    public Result<?> returnMaterial(@PathVariable Long recordId, @RequestBody ReturnRequest request) {
        requireOwnOrStaff(recordId);
        Map<String, Object> result = borrowService.returnMaterial(
                recordId, request.getReturnQuantity(), request.getReturnRemark(),
                request.getIsDamaged(), request.getDamageDescription());
        return Result.ok(result);
    }

    /**
     * 归还 / 申请延期只能操作自己的记录；教师与超管不受限（需要代为处理）。
     * 归属判断以服务端记录里存的借用人为准，不看请求体。
     */
    private void requireOwnOrStaff(Long recordId) {
        if (RoleGuard.isStaff()) return;
        BorrowRecord record = borrowService.getById(recordId);
        String me = UserContext.getUsername();
        boolean owner = me != null && (me.equals(record.getBorrowerUsername())
                || record.getBorrowerUsername() == null
                && (me.equals(record.getBorrower()) || record.getBorrower().equals(UserContext.getRealName())));
        if (!owner) {
            throw new RuntimeException("权限不足：只能操作自己的借用记录");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isEmpty()) return null;
        String normalized = value.trim();
        try {
            if (normalized.endsWith("Z") || normalized.matches(".*[+-]\\d{2}:?\\d{2}$")) {
                return OffsetDateTime.parse(normalized)
                        .atZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime();
            }
            if (normalized.length() > 19) {
                normalized = normalized.substring(0, 19);
            }
            return LocalDateTime.parse(normalized.replace(' ', 'T'));
        } catch (DateTimeParseException e) {
            throw new RuntimeException("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
        }
    }

    /**
     * 记录列表（分页）
     *
     * 归属过滤放在服务端：非教师/超管一律强制只查本人记录，忽略前端传入的 borrower。
     * 此前 borrower 完全由客户端控制，学生端"只能看自己的"只是前端加了个参数，
     * 直接调接口就能拉到全量借用记录，隐藏页签挡不住数据。
     */
    @GetMapping
    public Result<?> list(@RequestParam(required = false) String status,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String borrower,
                          @RequestParam(defaultValue = "false") boolean mine,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        String borrowerUsername = null;
        String borrowerName = borrower;
        if (!RoleGuard.isStaff() || mine) {
            borrowerUsername = UserContext.getUsername();
            borrowerName = null;
        }
        Page<BorrowRecord> result = borrowService.list(status, keyword, borrowerName,
                borrowerUsername, PageRequest.of(page - 1, size));
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("content", result.getContent());
        data.put("totalElements", result.getTotalElements());
        data.put("totalPages", result.getTotalPages());
        data.put("page", page);
        data.put("size", size);
        return Result.ok(data);
    }

    /** 待审批列表（教师用） */
    @GetMapping("/pending")
    public Result<?> pending() {
        RoleGuard.requireStaff();
        return Result.ok(borrowService.pendingApprovals());
    }

    /** 统计（非教师/超管只统计本人，与列表接口同一归属口径） */
    @GetMapping("/stats")
    public Result<?> stats() {
        String me = RoleGuard.isStaff() ? null : UserContext.getUsername();
        return Result.ok(borrowService.stats(me));
    }

    static class BorrowRequest {
        private Long materialId; private String borrower; private Integer quantity;
        private String purpose; private String expectedReturnTime;
        private String secondBorrower;
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
        public String getSecondBorrower() { return secondBorrower; }
        public void setSecondBorrower(String secondBorrower) { this.secondBorrower = secondBorrower; }
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

    static class BatchRequest {
        private String purpose;
        private String expectedReturnTime;
        private List<BorrowService.BatchLine> lines;
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        public String getExpectedReturnTime() { return expectedReturnTime; }
        public void setExpectedReturnTime(String expectedReturnTime) { this.expectedReturnTime = expectedReturnTime; }
        public List<BorrowService.BatchLine> getLines() { return lines; }
        public void setLines(List<BorrowService.BatchLine> lines) { this.lines = lines; }
    }
}

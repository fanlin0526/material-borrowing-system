package com.example.borrow.controller;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.entity.Category;
import com.example.borrow.entity.Material;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.CategoryRepository;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.service.MaterialService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入导出（替代原"导出SQL"调试功能）：
 * - GET  /api/export/materials  导出物资清单
 * - GET  /api/export/borrows    导出借用/领用记录
 * - POST /api/import/materials  上传Excel批量建档物资
 */
@RestController
public class DataExportController {

    @Autowired MaterialRepository materialRepository;
    @Autowired BorrowRecordRepository borrowRecordRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired MaterialService materialService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] MATERIAL_HEADERS =
            {"编号", "物资名称", "类别", "规格型号", "单位", "总数量", "可借数量", "存放位置", "预警阈值", "设备状态", "创建时间"};
    private static final String[] BORROW_HEADERS =
            {"物资名称", "类别", "借用人", "数量", "状态", "借用时间", "预计归还", "实际归还", "用途", "审批人", "逾期天数", "罚款(元)"};

    // ==================== 导出：物资清单 ====================

    @GetMapping("/api/export/materials")
    public void exportMaterials(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String category,
                                HttpServletResponse response) throws IOException {
        List<Material> list = materialService.list(keyword, category);
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("物资清单");
            writeHeader(sheet, MATERIAL_HEADERS, wb);
            int row = 1;
            for (Material m : list) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(nvl(m.getCode()));
                r.createCell(1).setCellValue(nvl(m.getName()));
                r.createCell(2).setCellValue(categoryName(m.getCategory()));
                r.createCell(3).setCellValue(nvl(m.getSpecification()));
                r.createCell(4).setCellValue(nvl(m.getUnit()));
                r.createCell(5).setCellValue(m.getTotalQuantity() != null ? m.getTotalQuantity() : 0);
                r.createCell(6).setCellValue(m.getAvailableQuantity() != null ? m.getAvailableQuantity() : 0);
                r.createCell(7).setCellValue(nvl(m.getLocation()));
                r.createCell(8).setCellValue(m.getWarningThreshold() != null ? m.getWarningThreshold() : 0);
                r.createCell(9).setCellValue(deviceStatusName(m.getDeviceStatus()));
                r.createCell(10).setCellValue(m.getCreateTime() != null ? m.getCreateTime().format(FMT) : "");
            }
            writeResponse(response, "物资清单_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx", wb);
        }
    }

    // ==================== 导出：借用/领用记录 ====================

    @GetMapping("/api/export/borrows")
    public void exportBorrows(@RequestParam(required = false) String status,
                              @RequestParam(required = false) String keyword,
                              HttpServletResponse response) throws IOException {
        List<BorrowRecord> list;
        if (status != null && !status.isEmpty()) {
            list = borrowRecordRepository.findByStatusOrderByBorrowTimeDesc(status);
        } else if (keyword != null && !keyword.isEmpty()) {
            list = borrowRecordRepository.search(keyword, org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else {
            list = borrowRecordRepository.findAllByOrderByBorrowTimeDesc();
        }
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("借用记录");
            writeHeader(sheet, BORROW_HEADERS, wb);
            int row = 1;
            for (BorrowRecord r : list) {
                Row rw = sheet.createRow(row++);
                rw.createCell(0).setCellValue(nvl(r.getMaterialName()));
                rw.createCell(1).setCellValue(categoryName(r.getMaterialCategory()));
                rw.createCell(2).setCellValue(nvl(r.getBorrower()));
                rw.createCell(3).setCellValue(r.getBorrowQuantity() != null ? r.getBorrowQuantity() : 0);
                rw.createCell(4).setCellValue(borrowStatusName(r.getStatus()));
                rw.createCell(5).setCellValue(r.getBorrowTime() != null ? r.getBorrowTime().format(FMT) : "");
                rw.createCell(6).setCellValue(r.getExpectedReturnTime() != null ? r.getExpectedReturnTime().format(FMT) : "");
                rw.createCell(7).setCellValue(r.getActualReturnTime() != null ? r.getActualReturnTime().format(FMT) : "");
                rw.createCell(8).setCellValue(nvl(r.getPurpose()));
                rw.createCell(9).setCellValue(nvl(r.getApprover()));
                rw.createCell(10).setCellValue(r.getOverdueDays() != null ? r.getOverdueDays() : 0);
                rw.createCell(11).setCellValue(r.getFineAmount() != null ? r.getFineAmount() : 0);
            }
            writeResponse(response, "借用记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx", wb);
        }
    }

    // ==================== 导入：物资批量建档 ====================

    /**
     * Excel 模板列（第一行为表头）：
     * 物资名称 | 类别编码(DEVICE/CONSUMABLE/WATER/REAGENT/GLASSWARE) | 规格型号 | 单位 | 数量 | 存放位置 | 预警阈值
     */
    @PostMapping("/api/import/materials")
    public Result<?> importMaterials(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要上传的Excel文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
            return Result.fail("仅支持 .xlsx / .xls 格式的Excel文件");
        }
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            int totalRows = sheet.getLastRowNum() + 1;
            if (totalRows <= 1) {
                return Result.fail("Excel 中没有数据行（第一行为表头）");
            }
            for (int i = 1; i < totalRows; i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                String name = cellStr(r, 0);
                if (name == null || name.trim().isEmpty()) {
                    errors.add("第" + (i + 1) + "行：物资名称为空，已跳过");
                    continue;
                }
                String catCode = cellStr(r, 1);
                if (catCode == null || !isValidCategory(catCode)) {
                    errors.add("第" + (i + 1) + "行：类别编码「" + nvl(catCode) + "」无效（应为 DEVICE/CONSUMABLE/WATER/REAGENT/GLASSWARE），已跳过");
                    continue;
                }
                int quantity = (int) cellNum(r, 4);
                if (quantity <= 0) {
                    errors.add("第" + (i + 1) + "行：数量必须大于0，已跳过");
                    continue;
                }
                try {
                    Material material = new Material();
                    material.setName(name.trim());
                    material.setCategory(catCode);
                    material.setSpecification(cellStr(r, 2));
                    material.setUnit(cellStr(r, 3));
                    material.setTotalQuantity(quantity);
                    material.setLocation(cellStr(r, 5));
                    double threshold = cellNum(r, 6);
                    material.setWarningThreshold(threshold > 0 ? (int) threshold : Math.max(2, quantity / 5));
                    materialService.save(material);
                    successCount++;
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行：导入失败（" + e.getMessage() + "），已跳过");
                }
            }
        } catch (Exception e) {
            return Result.fail("Excel 解析失败：" + e.getMessage());
        }
        String msg = "导入完成：成功 " + successCount + " 条";
        if (!errors.isEmpty()) {
            msg += "，失败/跳过 " + errors.size() + " 条";
            msg += "\n" + String.join("\n", errors.subList(0, Math.min(errors.size(), 10)));
        }
        return Result.ok(msg);
    }

    // ==================== 工具方法 ====================

    private void writeHeader(Sheet sheet, String[] headers, Workbook wb) {
        Row row = sheet.createRow(0);
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        for (int i = 0; i < headers.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(style);
            sheet.setColumnWidth(i, 16 * 256);
        }
        sheet.createFreezePane(0, 1);
    }

    private void writeResponse(HttpServletResponse response, String filename, Workbook wb) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + new String(filename.getBytes("UTF-8"), "ISO-8859-1") + "\"");
        wb.write(response.getOutputStream());
        response.getOutputStream().flush();
    }

    private String categoryName(String code) {
        if (code == null) return "";
        return categoryRepository.findByCode(code).map(Category::getName).orElse(code);
    }

    private boolean isValidCategory(String code) {
        return categoryRepository.findByCode(code).isPresent();
    }

    private String deviceStatusName(String s) {
        if (s == null) return "";
        switch (s) {
            case "AVAILABLE": return "可用";
            case "MAINTENANCE": return "维修中";
            case "SCRAPPED": return "已报废";
            default: return s;
        }
    }

    private String borrowStatusName(String s) {
        if (s == null) return "";
        switch (s) {
            case "PENDING": return "待审批";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已拒绝";
            case "PENDING_EXTEND": return "待审批延期";
            case "BORROWED": return "借用中";
            case "CONSUMED": return "已领用";
            case "RETURNED": return "已归还";
            case "OVERDUE": return "已逾期";
            case "DAMAGED": return "已损坏";
            default: return s;
        }
    }

    private String cellStr(Row row, int idx) {
        Cell c = row.getCell(idx);
        if (c == null) return null;
        String v;
        switch (c.getCellType()) {
            case STRING: v = c.getStringCellValue(); break;
            case NUMERIC: v = String.valueOf(c.getNumericCellValue()); break;
            case BOOLEAN: v = String.valueOf(c.getBooleanCellValue()); break;
            default: v = null;
        }
        return v == null ? null : v.trim();
    }

    private double cellNum(Row row, int idx) {
        Cell c = row.getCell(idx);
        if (c == null) return 0;
        switch (c.getCellType()) {
            case NUMERIC: return c.getNumericCellValue();
            case STRING:
                try { return Double.parseDouble(c.getStringCellValue().trim()); }
                catch (Exception e) { return 0; }
            default: return 0;
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }
}

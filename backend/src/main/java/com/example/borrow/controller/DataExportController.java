package com.example.borrow.controller;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.entity.Category;
import com.example.borrow.entity.Material;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.CategoryRepository;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.security.RoleGuard;
import com.example.borrow.service.MaterialService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    private static final String[] MATERIAL_IMPORT_HEADERS =
            {"物资名称", "类别", "规格型号", "单位", "数量", "存放位置", "预警阈值"};
    private static final String[] BORROW_HEADERS =
            {"物资名称", "类别", "借用人", "数量", "状态", "借用时间", "预计归还", "实际归还", "用途", "审批人", "逾期天数", "罚款(元)"};

    // ==================== 导出：物资清单 ====================

    @GetMapping("/api/export/materials")
    public void exportMaterials(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String category,
                                HttpServletResponse response) throws IOException {
        RoleGuard.requireStaff();
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
        RoleGuard.requireStaff();
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

    /** 下载 Excel 批量建档模板，附填写说明和示例数据。 */
    @GetMapping("/api/export/material-import-template")
    public void exportMaterialImportTemplate(HttpServletResponse response) throws IOException {
        RoleGuard.requireSuperAdmin();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet dataSheet = wb.createSheet("物资建档导入");
            writeHeader(dataSheet, MATERIAL_IMPORT_HEADERS, wb);

            Row sample1 = dataSheet.createRow(1);
            sample1.createCell(0).setCellValue("数字示波器");
            sample1.createCell(1).setCellValue("实验设备");
            sample1.createCell(2).setCellValue("DS1054Z 50MHz");
            sample1.createCell(3).setCellValue("台");
            sample1.createCell(4).setCellValue(2);
            sample1.createCell(5).setCellValue("A102电子实验室");
            sample1.createCell(6).setCellValue(1);

            Row sample2 = dataSheet.createRow(2);
            sample2.createCell(0).setCellValue("杜邦线");
            sample2.createCell(1).setCellValue("耗材");
            sample2.createCell(2).setCellValue("公母头 20cm 40芯");
            sample2.createCell(3).setCellValue("排");
            sample2.createCell(4).setCellValue(20);
            sample2.createCell(5).setCellValue("A103耗材柜");
            sample2.createCell(6).setCellValue(3);

            Sheet helpSheet = wb.createSheet("填写说明");
            String[][] help = {
                    {"项目", "说明"},
                    {"适用场景", "批量新增物资并建立期初库存；不会给已有物资补货"},
                    {"已有物资补货", "请到「采购入库」页面创建入库单"},
                    {"支持格式", ".xlsx 或 .xls，单个文件不超过 10MB"},
                    {"表头", "第一行必须是表头，顺序不可调整"},
                    {"物资名称", "必填，文本"},
                    {"类别", "必填，可填分类编码或中文名"},
                    {"数量", "必填，大于 0 的整数"},
                    {"规格型号、单位、存放位置", "可选"},
                    {"预警阈值", "可选；留空时按 max(2, 数量 × 20%) 计算"},
                    {"分类编码", "DEVICE、CONSUMABLE、WATER、REAGENT、GLASSWARE"},
                    {"中文分类", "实验设备、耗材、实验用水、化学试剂、玻璃器皿"}
            };
            for (int i = 0; i < help.length; i++) {
                Row row = helpSheet.createRow(i);
                row.createCell(0).setCellValue(help[i][0]);
                row.createCell(1).setCellValue(help[i][1]);
            }
            helpSheet.setColumnWidth(0, 24 * 256);
            helpSheet.setColumnWidth(1, 70 * 256);

            writeResponse(response, "物资建档导入模板.xlsx", wb);
        }
    }

    /**
     * Excel 模板列（第一行为表头）：
     * 物资名称 | 类别（编码或中文名） | 规格型号 | 单位 | 数量 | 存放位置 | 预警阈值
     */
    @PostMapping("/api/import/materials")
    public Result<?> importMaterials(@RequestParam("file") MultipartFile file) {
        RoleGuard.requireSuperAdmin();
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要上传的Excel文件");
        }
        String filename = file.getOriginalFilename();
        String lowerName = filename == null ? "" : filename.toLowerCase();
        if (!(lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls"))) {
            return Result.fail("仅支持 .xlsx / .xls 格式的Excel文件");
        }
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        // WorkbookFactory 同时支持 xlsx 与旧版 xls，不能固定使用 XSSFWorkbook。
        try (InputStream input = file.getInputStream();
             Workbook wb = WorkbookFactory.create(input)) {
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
                String rawCategory = cellStr(r, 1);
                String catCode = resolveCategoryCode(rawCategory);
                if (catCode == null) {
                    errors.add("第" + (i + 1) + "行：类别「" + nvl(rawCategory)
                            + "」无效（可填分类编码或中文分类名），已跳过");
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
        String msg = "导入完成：成功新增 " + successCount + " 条物资并建立期初库存";
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

    /** 导入时兼容分类编码和中文分类名，统一转换为系统编码。 */
    private String resolveCategoryCode(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String key = value.trim();
        Category byCode = categoryRepository.findByCode(key.toUpperCase()).orElse(null);
        if (byCode != null) return byCode.getCode();
        return categoryRepository.findAll().stream()
                .filter(c -> key.equals(c.getName()))
                .map(Category::getCode)
                .findFirst()
                .orElse(null);
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

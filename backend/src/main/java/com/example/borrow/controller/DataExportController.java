package com.example.borrow.controller;

import com.example.borrow.entity.*;
import com.example.borrow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class DataExportController {

    @Autowired UserRepository userRepository;
    @Autowired MaterialRepository materialRepository;
    @Autowired BorrowRecordRepository borrowRecordRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired SystemConfigRepository systemConfigRepository;
    @Autowired OperationLogRepository operationLogRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/export-sql")
    public Result<?> exportSQL() {
        try {
            PrintWriter w = new PrintWriter(new FileWriter("D:/material_borrow_mysql.sql"), true);
            w.println("-- 数据库导出: material_borrow (MySQL)");
            w.println("SET NAMES utf8mb4;");
            w.println();

            // t_user
            w.println("DROP TABLE IF EXISTS `t_user`;");
            w.println("CREATE TABLE `t_user` (");
            w.println("  `id` bigint NOT NULL AUTO_INCREMENT,");
            w.println("  `username` varchar(50) NOT NULL,");
            w.println("  `password` varchar(100) NOT NULL,");
            w.println("  `real_name` varchar(50) DEFAULT NULL,");
            w.println("  `role` varchar(20) NOT NULL,");
            w.println("  `phone` varchar(20) DEFAULT NULL,");
            w.println("  `avatar` varchar(255) DEFAULT NULL,");
            w.println("  `create_time` datetime DEFAULT NULL,");
            w.println("  PRIMARY KEY (`id`),");
            w.println("  UNIQUE KEY `uk_username` (`username`)");
            w.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            for (User u : userRepository.findAll()) {
                w.printf("INSERT INTO `t_user` VALUES (%d,'%s','%s','%s','%s',%s,%s,%s);\n",
                    u.getId(), esc(u.getUsername()), esc(u.getPassword()),
                    esc(u.getRealName()), esc(u.getRole()),
                    n(u.getPhone()), n(u.getAvatar()), ts(u.getCreateTime()));
            }
            w.println();

            // t_material
            w.println("DROP TABLE IF EXISTS `t_material`;");
            w.println("CREATE TABLE `t_material` (");
            w.println("  `id` bigint NOT NULL AUTO_INCREMENT,");
            w.println("  `code` varchar(50) DEFAULT NULL,");
            w.println("  `name` varchar(100) NOT NULL,");
            w.println("  `category` varchar(20) DEFAULT NULL,");
            w.println("  `specification` varchar(255) DEFAULT NULL,");
            w.println("  `unit` varchar(20) DEFAULT NULL,");
            w.println("  `total_quantity` int DEFAULT 0,");
            w.println("  `available_quantity` int DEFAULT 0,");
            w.println("  `location` varchar(100) DEFAULT NULL,");
            w.println("  `warning_threshold` int DEFAULT 0,");
            w.println("  `device_status` varchar(20) DEFAULT 'AVAILABLE',");
            w.println("  PRIMARY KEY (`id`)");
            w.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            for (Material m : materialRepository.findAll()) {
                w.printf("INSERT INTO `t_material` VALUES (%d,'%s','%s','%s','%s','%s',%d,%d,'%s',%d,'%s');\n",
                    m.getId(), esc(m.getCode()), esc(m.getName()), esc(m.getCategory()),
                    esc(m.getSpecification()), esc(m.getUnit()),
                    m.getTotalQuantity() != null ? m.getTotalQuantity() : 0,
                    m.getAvailableQuantity() != null ? m.getAvailableQuantity() : 0,
                    esc(m.getLocation()),
                    m.getWarningThreshold() != null ? m.getWarningThreshold() : 0,
                    m.getDeviceStatus() != null ? m.getDeviceStatus() : "AVAILABLE");
            }
            w.println();

            // t_borrow_record
            w.println("DROP TABLE IF EXISTS `t_borrow_record`;");
            w.println("CREATE TABLE `t_borrow_record` (");
            w.println("  `id` bigint NOT NULL AUTO_INCREMENT,");
            w.println("  `material_id` bigint NOT NULL,");
            w.println("  `material_name` varchar(100) DEFAULT NULL,");
            w.println("  `material_category` varchar(20) DEFAULT NULL,");
            w.println("  `borrower` varchar(50) NOT NULL,");
            w.println("  `borrow_quantity` int NOT NULL,");
            w.println("  `status` varchar(20) NOT NULL,");
            w.println("  `borrow_time` datetime NOT NULL,");
            w.println("  `expected_return_time` datetime DEFAULT NULL,");
            w.println("  `actual_return_time` datetime DEFAULT NULL,");
            w.println("  `purpose` varchar(500) DEFAULT NULL,");
            w.println("  `return_remark` varchar(500) DEFAULT NULL,");
            w.println("  `is_damaged` tinyint(1) DEFAULT 0,");
            w.println("  `damage_description` varchar(500) DEFAULT NULL,");
            w.println("  `approver` varchar(50) DEFAULT NULL,");
            w.println("  `approve_time` datetime DEFAULT NULL,");
            w.println("  `reject_reason` varchar(500) DEFAULT NULL,");
            w.println("  `extension_days` int DEFAULT NULL,");
            w.println("  `extension_reason` varchar(500) DEFAULT NULL,");
            w.println("  `extension_approved` tinyint(1) DEFAULT 0,");
            w.println("  `overdue_days` int DEFAULT NULL,");
            w.println("  `fine_amount` double DEFAULT 0,");
            w.println("  `create_time` datetime DEFAULT NULL,");
            w.println("  PRIMARY KEY (`id`)");
            w.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            for (BorrowRecord r : borrowRecordRepository.findAll()) {
                w.printf("INSERT INTO `t_borrow_record` VALUES (%d,%d,'%s','%s','%s',%d,'%s',%s,%s,%s,'%s','%s',%d,'%s','%s',%s,'%s',%d,'%s',%d,%d,%f,%s);\n",
                    r.getId(), r.getMaterialId(), esc(r.getMaterialName()), esc(r.getMaterialCategory()),
                    esc(r.getBorrower()), r.getBorrowQuantity(), esc(r.getStatus()),
                    ts(r.getBorrowTime()), ts(r.getExpectedReturnTime()), ts(r.getActualReturnTime()),
                    esc(r.getPurpose()), esc(r.getReturnRemark()),
                    r.getIsDamaged() != null && r.getIsDamaged() ? 1 : 0,
                    esc(r.getDamageDescription()), esc(r.getApprover()), ts(r.getApproveTime()),
                    esc(r.getRejectReason()), r.getExtensionDays() != null ? r.getExtensionDays() : 0,
                    esc(r.getExtensionReason()),
                    r.getExtensionApproved() != null && r.getExtensionApproved() ? 1 : 0,
                    r.getOverdueDays() != null ? r.getOverdueDays() : 0,
                    r.getFineAmount() != null ? r.getFineAmount() : 0,
                    ts(r.getCreateTime()));
            }
            w.println();

            // t_reservation
            w.println("DROP TABLE IF EXISTS `t_reservation`;");
            w.println("CREATE TABLE `t_reservation` (");
            w.println("  `id` bigint NOT NULL AUTO_INCREMENT,");
            w.println("  `material_id` bigint NOT NULL,");
            w.println("  `username` varchar(50) DEFAULT NULL,");
            w.println("  `real_name` varchar(50) DEFAULT NULL,");
            w.println("  `start_time` datetime DEFAULT NULL,");
            w.println("  `end_time` datetime DEFAULT NULL,");
            w.println("  `purpose` varchar(500) DEFAULT NULL,");
            w.println("  `attendee_count` int DEFAULT 1,");
            w.println("  `status` varchar(20) DEFAULT 'APPROVED',");
            w.println("  `recurring_rule` varchar(20) DEFAULT NULL,");
            w.println("  `create_time` datetime DEFAULT NULL,");
            w.println("  PRIMARY KEY (`id`)");
            w.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            for (Reservation r : reservationRepository.findAll()) {
                w.printf("INSERT INTO `t_reservation` VALUES (%d,%d,'%s','%s',%s,%s,'%s',%d,'%s','%s',%s);\n",
                    r.getId(), r.getMaterialId(), esc(r.getUsername()), esc(r.getRealName()),
                    ts(r.getStartTime()), ts(r.getEndTime()), esc(r.getPurpose()),
                    r.getAttendeeCount() != null ? r.getAttendeeCount() : 1,
                    esc(r.getStatus()), esc(r.getRecurringRule()), ts(r.getCreateTime()));
            }
            w.println();

            // t_notification
            w.println("DROP TABLE IF EXISTS `t_notification`;");
            w.println("CREATE TABLE `t_notification` (");
            w.println("  `id` bigint NOT NULL AUTO_INCREMENT,");
            w.println("  `username` varchar(50) DEFAULT NULL,");
            w.println("  `title` varchar(200) DEFAULT NULL,");
            w.println("  `content` text,");
            w.println("  `type` varchar(20) DEFAULT NULL,");
            w.println("  `is_read` tinyint(1) DEFAULT 0,");
            w.println("  `create_time` datetime DEFAULT NULL,");
            w.println("  PRIMARY KEY (`id`)");
            w.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            for (Notification n : notificationRepository.findAll()) {
                w.printf("INSERT INTO `t_notification` VALUES (%d,'%s','%s','%s','%s',%d,%s);\n",
                    n.getId(), esc(n.getUsername()), esc(n.getTitle()), esc(n.getContent()),
                    esc(n.getType()), n.getIsRead() != null && n.getIsRead() ? 1 : 0,
                    ts(n.getCreateTime()));
            }
            w.println();

            // t_system_config
            w.println("DROP TABLE IF EXISTS `t_system_config`;");
            w.println("CREATE TABLE `t_system_config` (");
            w.println("  `id` bigint NOT NULL AUTO_INCREMENT,");
            w.println("  `config_key` varchar(100) NOT NULL,");
            w.println("  `config_value` varchar(500) DEFAULT NULL,");
            w.println("  `description` varchar(255) DEFAULT NULL,");
            w.println("  PRIMARY KEY (`id`),");
            w.println("  UNIQUE KEY `uk_config_key` (`config_key`)");
            w.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            for (SystemConfig c : systemConfigRepository.findAll()) {
                w.printf("INSERT INTO `t_system_config` VALUES (%d,'%s','%s','%s');\n",
                    c.getId(), esc(c.getConfigKey()), esc(c.getConfigValue()), esc(c.getDescription()));
            }
            w.println();

            // t_operation_log
            w.println("DROP TABLE IF EXISTS `t_operation_log`;");
            w.println("CREATE TABLE `t_operation_log` (");
            w.println("  `id` bigint NOT NULL AUTO_INCREMENT,");
            w.println("  `username` varchar(50) NOT NULL,");
            w.println("  `real_name` varchar(50) DEFAULT NULL,");
            w.println("  `action` varchar(30) NOT NULL,");
            w.println("  `detail` varchar(500) DEFAULT NULL,");
            w.println("  `target_type` varchar(50) DEFAULT NULL,");
            w.println("  `target_id` bigint DEFAULT NULL,");
            w.println("  `ip` varchar(50) DEFAULT NULL,");
            w.println("  `create_time` datetime DEFAULT NULL,");
            w.println("  PRIMARY KEY (`id`)");
            w.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            for (OperationLog l : operationLogRepository.findAll()) {
                w.printf("INSERT INTO `t_operation_log` VALUES (%d,'%s','%s','%s','%s','%s',%s,'%s',%s);\n",
                    l.getId(), esc(l.getUsername()), esc(l.getRealName()), esc(l.getAction()),
                    esc(l.getDetail()), esc(l.getTargetType()),
                    l.getTargetId() != null ? l.getTargetId().toString() : "NULL",
                    esc(l.getIp()), ts(l.getCreateTime()));
            }

            w.close();
            return Result.ok("SQL 已导出到 D:/material_borrow_mysql.sql");
        } catch (Exception e) {
            return Result.fail("导出失败: " + e.getMessage());
        }
    }

    private String esc(String s) { return s == null ? "NULL" : "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'"; }
    private String n(String s) { return s == null || s.isEmpty() ? "NULL" : "'" + s.replace("'", "\\'") + "'"; }
    private String ts(java.time.LocalDateTime t) { return t == null ? "NULL" : "'" + t.format(FMT) + "'"; }
}

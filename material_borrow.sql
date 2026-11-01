/*
 Navicat Premium Data Transfer

 Source Server         : 本机数据库
 Source Server Type    : MySQL
 Source Server Version : 80012
 Source Host           : localhost:3306
 Source Schema         : material_borrow

 Target Server Type    : MySQL
 Target Server Version : 80012
 File Encoding         : 65001

 Date: 28/10/2026 15:42:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_attachment
-- ----------------------------
DROP TABLE IF EXISTS `t_attachment`;
CREATE TABLE `t_attachment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `file_size` bigint(20) NULL DEFAULT NULL,
  `ref_id` bigint(20) NOT NULL,
  `ref_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `stored_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `uploader` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_attachment
-- ----------------------------

-- ----------------------------
-- Table structure for t_borrow_record
-- ----------------------------
DROP TABLE IF EXISTS `t_borrow_record`;
CREATE TABLE `t_borrow_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `actual_return_time` datetime NULL DEFAULT NULL,
  `borrow_quantity` int(11) NOT NULL,
  `borrow_time` datetime NOT NULL,
  `borrower` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `damage_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `expected_return_time` datetime NULL DEFAULT NULL,
  `is_damaged` tinyint(1) NULL DEFAULT NULL,
  `material_category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `material_id` bigint(20) NOT NULL,
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `purpose` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `return_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `approve_time` datetime NULL DEFAULT NULL,
  `approver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `extension_approved` tinyint(1) NULL DEFAULT NULL,
  `extension_days` int(11) NULL DEFAULT NULL,
  `extension_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `fine_amount` double NULL DEFAULT NULL,
  `overdue_days` int(11) NULL DEFAULT NULL,
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `returned_quantity` int(11) NULL DEFAULT NULL,
  `approval_stage` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `borrower_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `estimated_fee` double NULL DEFAULT NULL,
  `mentor_approve_time` datetime(6) NULL DEFAULT NULL,
  `mentor_approver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `request_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `second_borrower` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_borrow_record
-- ----------------------------
INSERT INTO `t_borrow_record` VALUES (1, '2026-10-11 22:19:28', 1, '2026-07-28 13:14:35', '张明', '2026-07-30 13:14:35', NULL, '2026-08-04 13:14:35', 0, 'DEVICE', 1, '数字示波器', '毕业设计实验-信号采集系统', '', 'RETURNED', NULL, NULL, 0, NULL, NULL, 0, 68, NULL, 1, NULL, 'zhangming', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (2, NULL, 2, '2026-07-29 13:14:35', '李华', '2026-07-30 13:14:35', NULL, '2026-08-05 13:14:35', 0, 'DEVICE', 3, '数字万用表', '课程实验-模拟电路', NULL, 'OVERDUE', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'lihua', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (3, NULL, 3, '2026-07-27 13:14:35', '王芳', '2026-07-30 13:14:35', NULL, '2026-08-03 13:14:35', 0, 'CONSUMABLE', 13, 'U盘 32GB', '毕业设计-水质检测', NULL, 'OVERDUE', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'wangfang', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (4, NULL, 2, '2026-07-25 13:14:35', '赵强', '2026-07-30 13:14:35', NULL, '2026-08-01 13:14:35', 0, 'CONSUMABLE', 14, '透明胶带', '实验室日常维护', NULL, 'OVERDUE', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'zhaoqiang', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (5, '2026-10-11 22:19:25', 5, '2026-07-29 13:14:35', '张明', '2026-07-30 13:14:35', NULL, '2026-08-05 13:14:35', 0, 'CONSUMABLE', 17, '热缩管套装', '毕业设计实验-信号采集系统', '', 'RETURNED', NULL, NULL, 0, NULL, NULL, 0, 67, NULL, 5, NULL, 'zhangming', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (6, NULL, 2, '2026-07-26 13:14:35', '刘洋', '2026-07-30 13:14:35', NULL, '2026-08-02 13:14:35', 0, 'CONSUMABLE', 20, '尼龙扎带', '教学演示', NULL, 'OVERDUE', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'liuyang', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (7, NULL, 3, '2026-07-28 13:14:35', '王芳', '2026-07-30 13:14:35', NULL, '2026-08-04 13:14:35', 0, 'WATER', 28, '去离子水', '毕业设计-水质检测', NULL, 'OVERDUE', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'wangfang', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (8, NULL, 5, '2026-07-27 13:14:35', '李华', '2026-07-30 13:14:35', NULL, '2026-08-03 13:14:35', 0, 'WATER', 29, '蒸馏水', '课程实验-化学分析', NULL, 'OVERDUE', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'lihua', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (9, NULL, 1, '2026-07-29 13:14:35', '赵强', '2026-07-30 13:14:35', NULL, '2026-08-05 13:14:35', 0, 'REAGENT', 38, '硫酸铜', '科研课题-传感器标定', NULL, 'OVERDUE', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'zhaoqiang', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (10, NULL, 4, '2026-07-28 13:14:35', '刘洋', '2026-07-30 13:14:35', NULL, '2026-08-04 13:14:35', 0, 'GLASSWARE', 43, '烧杯', '课外创新项目', NULL, 'OVERDUE', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'liuyang', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (11, NULL, 2, '2026-07-24 13:14:35', '王芳', '2026-07-30 13:14:35', NULL, '2026-07-31 13:14:35', 0, 'GLASSWARE', 44, '量筒', '毕业设计-水质检测', NULL, 'OVERDUE', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'wangfang', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (12, '2026-10-11 22:19:26', 1, '2026-07-29 13:14:35', '张明', '2026-07-30 13:14:35', NULL, '2026-08-05 13:14:35', 0, 'GLASSWARE', 48, '移液管', '毕业设计实验-信号采集系统', '', 'RETURNED', NULL, NULL, 0, NULL, NULL, 0, 67, NULL, 1, NULL, 'zhangming', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (13, '2026-07-22 13:14:35', 1, '2026-07-20 13:14:35', '陈思', '2026-07-30 13:14:35', NULL, '2026-07-27 13:14:35', 0, 'DEVICE', 2, '直流稳压电源', '课程实验-模拟电路', '完好归还', 'RETURNED', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'chensi', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (14, '2026-07-18 13:14:35', 1, '2026-07-16 13:14:35', '刘洋', '2026-07-30 13:14:35', NULL, '2026-07-23 13:14:35', 0, 'DEVICE', 4, '信号发生器', '竞赛备赛-电子设计大赛', '完好归还', 'RETURNED', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'liuyang', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (15, '2026-07-25 13:14:35', 2, '2026-07-23 13:14:35', '王芳', '2026-07-30 13:14:35', NULL, '2026-07-30 13:14:35', 0, 'CONSUMABLE', 15, '杜邦线', '毕业设计-水质检测', '完好归还', 'RETURNED', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'wangfang', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (16, '2026-07-12 13:14:35', 5, '2026-07-10 13:14:35', '赵强', '2026-07-30 13:14:35', NULL, '2026-07-17 13:14:35', 0, 'GLASSWARE', 47, '滴定管', '课程实验-化学分析', '完好归还', 'RETURNED', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'zhaoqiang', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (17, '2026-07-27 13:14:35', 2, '2026-07-25 13:14:35', '张明', '2026-07-30 13:14:35', NULL, '2026-08-01 13:14:35', 0, 'REAGENT', 37, '氯化钠', '毕业设计实验-信号采集系统', '完好归还', 'RETURNED', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'zhangming', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (18, '2026-07-24 13:14:35', 3, '2026-07-22 13:14:35', '李华', '2026-07-30 13:14:35', NULL, '2026-07-29 13:14:35', 0, 'CONSUMABLE', 27, '电池 AA', '课程实验-化学分析', '完好归还', 'RETURNED', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL, 'lihua', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (19, '2026-10-11 22:19:23', 1, '2026-09-17 19:00:00', '张明', '2026-09-17 20:04:12', NULL, '2026-09-17 21:00:00', 0, 'DEVICE', 7, '热风枪', '预约借用（预约单#5）', '', 'RETURNED', '2026-09-17 20:04:12', '超级管理员', 0, NULL, NULL, 0, 24, NULL, 1, NULL, 'zhangming', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (20, NULL, 1, '2026-09-17 20:04:49', '张明', '2026-09-17 20:04:49', NULL, NULL, 0, 'REAGENT', 41, 'EDTA二钠盐', '', NULL, 'REJECTED', '2026-10-23 19:21:20', '超级管理员', 0, NULL, NULL, 0, NULL, '', 0, NULL, 'zhangming', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (21, NULL, 1, '2026-10-11 22:12:52', '张明', '2026-10-11 22:12:52', NULL, '2026-10-20 16:00:00', 0, 'DEVICE', 5, '防静电焊接台', '实验', NULL, 'OVERDUE', '2026-10-11 22:13:12', '王老师', 1, NULL, '', 0, NULL, NULL, 0, NULL, 'zhangming', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (22, '2026-10-23 19:42:21', 1, '2026-10-12 07:30:00', '王老师', '2026-10-11 22:16:13', NULL, '2026-10-12 10:30:00', 0, 'DEVICE', 2, '直流稳压电源', '实验（预约单#6）', '预约时段结束，系统自动结单，无需手动归还', 'RETURNED', '2026-10-11 22:16:13', '王老师', 0, NULL, NULL, 0, NULL, NULL, 1, NULL, 'teacher', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (23, '2026-10-23 19:42:21', 1, '2026-10-14 08:00:00', '超级管理员', '2026-10-13 13:22:15', NULL, '2026-10-14 10:00:00', 0, 'DEVICE', 1, '数字示波器', '预约借用（预约单#9）', '预约时段结束，系统自动结单，无需手动归还', 'RETURNED', '2026-10-13 13:22:15', '超级管理员', 0, NULL, NULL, 0, NULL, NULL, 1, NULL, 'superadmin', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (47, NULL, 1, '2026-10-23 19:43:35', '王老师', '2026-10-23 19:43:35', NULL, '2026-10-25 19:43:35', 0, 'DEVICE', 1, '数字示波器', 'teacher test', NULL, 'OVERDUE', '2026-10-23 19:43:35', '王老师', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'teacher', 100, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (48, NULL, 1, '2026-10-23 19:43:35', '王老师', '2026-10-23 19:43:35', NULL, NULL, 0, 'CONSUMABLE', 13, 'U盘 32GB', 'teacher test', NULL, 'CONSUMED', '2026-10-23 19:43:35', '王老师', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'teacher', 2, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (49, NULL, 1, '2026-10-23 19:43:56', '超级管理员', '2026-10-23 19:43:56', NULL, '2026-10-25 19:43:55', 0, 'DEVICE', 1, '数字示波器', 'admin test', NULL, 'OVERDUE', '2026-10-23 19:43:56', '超级管理员', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'superadmin', 100, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (50, NULL, 1, '2026-10-23 19:43:56', '超级管理员', '2026-10-23 19:43:56', NULL, NULL, 0, 'CONSUMABLE', 13, 'U盘 32GB', 'admin test', NULL, 'CONSUMED', '2026-10-23 19:43:56', '超级管理员', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'superadmin', 2, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (51, NULL, 1, '2026-10-23 19:44:41', '王老师', '2026-10-23 19:44:41', NULL, '2026-10-25 19:44:41', 0, 'DEVICE', 1, '数字示波器', '教师单项回归', NULL, 'OVERDUE', '2026-10-23 19:44:41', '王老师', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'teacher', 100, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (52, '2026-10-23 19:44:53', 1, '2026-10-23 19:44:53', '王老师', '2026-10-23 19:44:53', NULL, '2026-10-25 19:44:52', 0, 'DEVICE', 2, '直流稳压电源', '教师单项回归', '回归归还', 'RETURNED', '2026-10-23 19:44:53', '王老师', 0, NULL, NULL, 0, 0, NULL, 1, 'STAFF', 'teacher', 100, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (53, NULL, 1, '2026-10-23 19:44:53', '王老师', '2026-10-23 19:44:53', NULL, NULL, 0, 'CONSUMABLE', 13, 'U盘 32GB', '教师单项回归', NULL, 'CONSUMED', '2026-10-23 19:44:53', '王老师', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'teacher', 2, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (54, '2026-10-23 19:44:53', 1, '2026-10-23 19:44:53', '超级管理员', '2026-10-23 19:44:53', NULL, '2026-10-25 19:44:52', 0, 'DEVICE', 2, '直流稳压电源', '管理员单项回归', '回归归还', 'RETURNED', '2026-10-23 19:44:53', '超级管理员', 0, NULL, NULL, 0, 0, NULL, 1, 'STAFF', 'superadmin', 100, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (55, '2026-10-23 19:44:53', 1, '2026-10-23 19:44:53', '张明', '2026-10-23 19:44:53', NULL, '2026-10-25 19:44:52', 0, 'DEVICE', 2, '直流稳压电源', '学生审批回归', '学生归还', 'RETURNED', '2026-10-23 19:44:53', '王老师', 0, NULL, NULL, 0, 0, NULL, 1, 'STAFF', 'zhangming', 100, '2026-10-23 19:44:53.104305', '王老师', NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (56, '2026-10-26 23:48:03', 1, '2026-10-26 13:00:00', '张明', '2026-10-23 19:44:53', NULL, '2026-10-26 15:00:00', 0, 'DEVICE', 2, '直流稳压电源', '预约回归（预约单#12）', '预约时段结束，系统自动结单，无需手动归还', 'RETURNED', '2026-10-23 19:44:53', '王老师', 0, NULL, NULL, 0, NULL, NULL, 1, NULL, 'zhangming', 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (57, '2026-10-24 19:44:44', 1, '2026-10-24 07:00:00', '张明', '2026-10-23 19:45:01', NULL, '2026-10-24 09:00:00', 0, 'DEVICE', 4, '信号发生器', '实验（预约单#11）', '预约时段结束，系统自动结单，无需手动归还', 'RETURNED', '2026-10-23 19:45:01', '王老师', 0, NULL, NULL, 0, NULL, NULL, 1, NULL, 'zhangming', 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (58, NULL, 1, '2026-10-23 19:46:15', '李华', '2026-10-23 19:46:15', NULL, '2026-10-25 19:46:15', 0, 'DEVICE', 207, '数字示波器', '撤回回归', NULL, 'CANCELLED', NULL, NULL, 0, NULL, NULL, 0, NULL, '申请人主动撤回', 0, 'MENTOR', 'lihua', 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (59, '2026-10-23 19:46:15', 1, '2026-10-23 19:46:15', '李华', '2026-10-23 19:46:15', NULL, '2026-10-27 19:46:15', 0, 'DEVICE', 207, '数字示波器', '已完善用途', '结单', 'RETURNED', '2026-10-23 19:46:15', '王老师', 1, NULL, '实验延期', 0, 0, NULL, 1, 'STAFF', 'lihua', 0, '2026-10-23 19:46:15.254429', '王老师', NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (60, NULL, 1, '2026-10-23 19:47:55', '王老师', '2026-10-23 19:47:55', NULL, '2026-10-25 19:47:54', 0, 'DEVICE', 2, '直流稳压电源', 'final borrow', NULL, 'OVERDUE', '2026-10-23 19:47:55', '王老师', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'teacher', 100, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (61, NULL, 1, '2026-10-23 19:47:55', '王老师', '2026-10-23 19:47:55', NULL, NULL, 0, 'CONSUMABLE', 13, 'U盘 32GB', 'final consume', NULL, 'CONSUMED', '2026-10-23 19:47:55', '王老师', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'teacher', 2, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (62, NULL, 1, '2026-10-23 19:53:33', '超级管理员', '2026-10-23 19:53:33', NULL, '2026-10-24 10:00:00', 0, 'DEVICE', 2, '直流稳压电源', '实验2', NULL, 'OVERDUE', '2026-10-23 19:53:33', '超级管理员', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'superadmin', 50, NULL, NULL, 'BR202610231953333834', NULL);
INSERT INTO `t_borrow_record` VALUES (63, NULL, 1, '2026-10-23 19:53:33', '超级管理员', '2026-10-23 19:53:33', NULL, '2026-10-24 10:00:00', 0, 'DEVICE', 3, '数字万用表', '实验2', NULL, 'OVERDUE', '2026-10-23 19:53:33', '超级管理员', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'superadmin', 50, NULL, NULL, 'BR202610231953333834', NULL);
INSERT INTO `t_borrow_record` VALUES (64, NULL, 1, '2026-10-23 19:53:33', '超级管理员', '2026-10-23 19:53:33', NULL, '2026-10-24 10:00:00', 0, 'DEVICE', 4, '信号发生器', '实验2', NULL, 'OVERDUE', '2026-10-23 19:53:33', '超级管理员', 0, NULL, NULL, 0, NULL, NULL, 0, 'STAFF', 'superadmin', 50, NULL, NULL, 'BR202610231953333834', NULL);
INSERT INTO `t_borrow_record` VALUES (65, NULL, 1, '2026-10-24 19:45:36', '张明', '2026-10-24 19:45:36', NULL, '2026-10-25 09:00:00', 0, 'DEVICE', 2, '直流稳压电源', '', NULL, 'PENDING', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, 0, 'MENTOR', 'zhangming', 50, NULL, NULL, 'BR202610241945359936', NULL);
INSERT INTO `t_borrow_record` VALUES (66, NULL, 1, '2026-10-24 19:45:36', '张明', '2026-10-24 19:45:36', NULL, '2026-10-25 09:00:00', 0, 'DEVICE', 3, '数字万用表', '', NULL, 'PENDING', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, 0, 'MENTOR', 'zhangming', 50, NULL, NULL, 'BR202610241945359936', NULL);
INSERT INTO `t_borrow_record` VALUES (67, NULL, 1, '2026-10-25 09:38:00', '张明', '2026-10-25 09:38:00', NULL, '2026-10-26 09:30:00', 0, 'DEVICE', 2, '直流稳压电源', '实验', NULL, 'PENDING', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, 0, 'MENTOR', 'zhangming', 50, NULL, NULL, 'BR202610250937598209', NULL);
INSERT INTO `t_borrow_record` VALUES (68, NULL, 1, '2026-10-25 09:38:00', '张明', '2026-10-25 09:38:00', NULL, '2026-10-26 09:30:00', 0, 'DEVICE', 3, '数字万用表', '实验', NULL, 'PENDING', NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, 0, 'MENTOR', 'zhangming', 50, NULL, NULL, 'BR202610250937598209', NULL);
INSERT INTO `t_borrow_record` VALUES (69, '2026-10-26 23:48:03', 1, '2026-10-25 15:00:00', '张明', '2026-10-25 09:42:30', NULL, '2026-10-25 17:00:00', 0, 'DEVICE', 3, '数字万用表', '实验3（第1次）（预约单#19）', '预约时段结束，系统自动结单，无需手动归还', 'RETURNED', '2026-10-25 09:42:30', '超级管理员', 0, NULL, NULL, 0, NULL, NULL, 1, NULL, 'zhangming', 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (70, NULL, 1, '2026-10-28 11:00:00', '张明', '2026-10-27 16:26:58', NULL, '2026-10-28 19:30:00', 0, 'DEVICE', 7, '热风枪', '实验（预约单#22）', NULL, 'BORROWED', '2026-10-27 16:26:58', '超级管理员', 0, NULL, NULL, 0, NULL, NULL, 0, NULL, 'zhangming', 0, NULL, NULL, NULL, NULL);
INSERT INTO `t_borrow_record` VALUES (71, NULL, 1, '2026-10-29 08:00:00', '张明', '2026-10-27 16:29:10', NULL, '2026-10-29 09:00:00', 0, 'DEVICE', 2, '直流稳压电源', '实验（预约单#23）', NULL, 'BORROWED', '2026-10-27 16:29:10', '王老师', 0, NULL, NULL, 0, NULL, NULL, 0, NULL, 'zhangming', 0, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for t_category
-- ----------------------------
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `returnable` bit(1) NOT NULL,
  `sort_order` int(11) NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_7pm8bgv42kscq1vy93me64xia`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_category
-- ----------------------------
INSERT INTO `t_category` VALUES (1, 'DEVICE', '2026-07-30 11:23:53.505252', '示波器、电源、万用表等电子实验设备', '实验设备', b'1', 1, '2026-07-30 11:23:53.505252');
INSERT INTO `t_category` VALUES (2, 'CONSUMABLE', '2026-07-30 11:23:53.537032', 'U盘、胶带、杜邦线、焊锡丝等消耗品', '耗材', b'0', 2, '2026-07-30 11:23:53.537032');
INSERT INTO `t_category` VALUES (3, 'WATER', '2026-07-30 11:23:53.552341', '去离子水、蒸馏水、超纯水', '实验用水', b'0', 3, '2026-07-30 11:23:53.552341');
INSERT INTO `t_category` VALUES (4, 'REAGENT', '2026-07-30 11:23:53.567402', '酸碱盐试剂、指示剂、有机溶剂', '化学试剂', b'0', 4, '2026-07-30 11:23:53.567402');
INSERT INTO `t_category` VALUES (5, 'GLASSWARE', '2026-07-30 11:23:53.582890', '烧杯、量筒、试管、滴定管等玻璃制品', '玻璃器皿', b'1', 5, '2026-07-30 11:23:53.582890');

-- ----------------------------
-- Table structure for t_material
-- ----------------------------
DROP TABLE IF EXISTS `t_material`;
CREATE TABLE `t_material`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `available_quantity` int(11) NOT NULL,
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `device_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `total_quantity` int(11) NOT NULL,
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `warning_threshold` int(11) NULL DEFAULT NULL,
  `approval_required` bit(1) NULL DEFAULT NULL,
  `borrowable` bit(1) NULL DEFAULT NULL,
  `cabinet_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cas_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `hazardous` bit(1) NULL DEFAULT NULL,
  `hourly_price` double NULL DEFAULT NULL,
  `needs_training` bit(1) NULL DEFAULT NULL,
  `next_maintenance_date` date NULL DEFAULT NULL,
  `unit_price` double NULL DEFAULT NULL,
  `valid_until` date NULL DEFAULT NULL,
  `version` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 210 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_material
-- ----------------------------
INSERT INTO `t_material` VALUES (1, 1, 'DEVICE', 'EQ001', '2026-07-23 16:18:05', 'AVAILABLE', 'A102电子实验室', '数字示波器', NULL, '普源 DS1054Z 50MHz', 6, '台', '2026-10-25 09:41:53', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 6);
INSERT INTO `t_material` VALUES (2, 5, 'DEVICE', 'EQ002', '2026-07-23 16:18:05', 'AVAILABLE', 'A102电子实验室', '直流稳压电源', NULL, '普源 DP832A 三通道', 8, '台', '2026-10-27 16:29:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 13);
INSERT INTO `t_material` VALUES (3, 11, 'DEVICE', 'EQ003', '2026-07-23 16:18:05', 'AVAILABLE', 'A101基础实验室', '数字万用表', NULL, '福禄克 Fluke 17B+', 16, '个', '2026-10-26 23:48:03', 3, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 4);
INSERT INTO `t_material` VALUES (4, 3, 'DEVICE', 'EQ004', '2026-07-23 16:18:05', 'AVAILABLE', 'A102电子实验室', '信号发生器', NULL, '普源 DG1022Z 25MHz', 4, '台', '2026-10-24 19:44:44', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 4);
INSERT INTO `t_material` VALUES (5, 3, 'DEVICE', 'EQ005', '2026-07-23 16:18:05', 'AVAILABLE', 'A103焊接实训室', '防静电焊接台', NULL, '白光 BK881 焊台', 6, '台', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 1);
INSERT INTO `t_material` VALUES (6, 3, 'DEVICE', 'EQ006', '2026-07-23 16:18:05', 'AVAILABLE', 'A102电子实验室', '逻辑分析仪', NULL, '梦源 LA1010 16通道', 3, '台', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 1);
INSERT INTO `t_material` VALUES (7, 4, 'DEVICE', 'EQ007', '2026-07-23 16:18:05', 'AVAILABLE', 'A103焊接实训室', '热风枪', NULL, '快克 850A+', 5, '把', '2026-10-27 16:26:58', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 2);
INSERT INTO `t_material` VALUES (8, 2, 'DEVICE', 'EQ008', '2026-07-23 16:18:05', 'AVAILABLE', 'A104通信实验室', '频谱分析仪', NULL, '安捷伦 N9000A 3GHz', 2, '台', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 1);
INSERT INTO `t_material` VALUES (9, 3, 'DEVICE', 'EQ009', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学实验室', '电子天平', NULL, '梅特勒 ME204 220g/0.1mg', 3, '台', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 1);
INSERT INTO `t_material` VALUES (10, 2, 'DEVICE', 'EQ010', '2026-07-23 16:18:05', 'AVAILABLE', 'B202生物实验室', '离心机', NULL, '湘仪 H1650 16500rpm', 2, '台', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 1);
INSERT INTO `t_material` VALUES (11, 4, 'DEVICE', 'EQ011', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学实验室', 'pH计', NULL, '梅特勒 FE28 0.01精度', 4, '台', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 1);
INSERT INTO `t_material` VALUES (12, 6, 'DEVICE', 'EQ012', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学实验室', '磁力搅拌器', NULL, 'IKA C-MAG 5L', 6, '台', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 1);
INSERT INTO `t_material` VALUES (13, 2, 'CONSUMABLE', 'CS001', '2026-07-23 16:18:05', 'AVAILABLE', 'A101耗材柜', 'U盘 32GB', NULL, '闪迪 CZ48 32GB USB3.0', 12, '个', '2026-10-23 19:47:55', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 6);
INSERT INTO `t_material` VALUES (14, 26, 'CONSUMABLE', 'CS002', '2026-07-23 16:18:05', 'AVAILABLE', 'A101耗材柜', '透明胶带', NULL, '3M 18mm宽 透明', 30, '卷', '2026-10-23 19:43:05', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (15, 50, 'CONSUMABLE', 'CS003', '2026-07-23 16:18:05', 'AVAILABLE', 'A103耗材柜', '杜邦线', NULL, '公母头 20cm 40芯', 50, '排', '2026-10-23 19:43:05', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (16, 15, 'CONSUMABLE', 'CS004', '2026-07-23 16:18:05', 'AVAILABLE', 'A103耗材柜', '焊锡丝', NULL, '千住 0.8mm 含松香 1kg', 15, '卷', '2026-10-23 19:43:05', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (17, 20, 'CONSUMABLE', 'CS005', '2026-07-23 16:18:05', 'AVAILABLE', 'A103耗材柜', '热缩管套装', NULL, '2-10mm 各规格 600mm长', 25, '套', '2026-10-23 19:43:05', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (18, 50, 'CONSUMABLE', 'CS006', '2026-07-23 16:18:05', 'AVAILABLE', 'A101耗材柜', '实验记录本', NULL, 'A4 80页 方格内页', 50, '本', '2026-10-23 19:43:05', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (19, 200, 'CONSUMABLE', 'CS007', '2026-07-23 16:18:05', 'AVAILABLE', 'A101耗材柜', '标签纸', NULL, '60×40mm 白色不干胶', 200, '张', '2026-10-23 19:43:05', 40, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (20, 36, 'CONSUMABLE', 'CS008', '2026-07-23 16:18:05', 'AVAILABLE', 'A103耗材柜', '尼龙扎带', NULL, '3×100mm 黑色 100根/包', 40, '包', '2026-10-23 19:43:05', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (21, 20, 'CONSUMABLE', 'CS009', '2026-07-23 16:18:05', 'AVAILABLE', 'A103耗材柜', '松香助焊剂', NULL, '固体松香 50g', 20, '盒', '2026-10-23 19:43:05', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (22, 30, 'CONSUMABLE', 'CS010', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜', '无水乙醇', NULL, '分析纯 500ml', 30, '瓶', '2026-10-23 19:43:05', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (23, 25, 'CONSUMABLE', 'CS011', '2026-07-23 16:18:05', 'AVAILABLE', 'B201耗材柜', '脱脂棉', NULL, '医用级 100g/包', 25, '包', '2026-10-23 19:43:05', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (24, 20, 'CONSUMABLE', 'CS012', '2026-07-23 16:18:05', 'AVAILABLE', 'B201耗材柜', '滤纸', NULL, '定性 Φ9cm 100张/盒', 20, '盒', '2026-10-23 19:43:05', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (25, 15, 'CONSUMABLE', 'CS013', '2026-07-23 16:18:05', 'AVAILABLE', 'B202防护用品柜', '乳胶手套', NULL, '一次性 无粉 M码 100只', 15, '盒', '2026-10-23 19:43:05', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (26, 20, 'CONSUMABLE', 'CS014', '2026-07-23 16:18:05', 'AVAILABLE', 'B202防护用品柜', '口罩', NULL, '一次性医用 50只/盒', 20, '盒', '2026-10-23 19:43:05', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (27, 30, 'CONSUMABLE', 'CS015', '2026-07-23 16:18:05', 'AVAILABLE', 'A101耗材柜', '电池 AA', NULL, '南孚 5号 1.5V 4粒装', 30, '组', '2026-10-23 19:43:05', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (28, 34, 'WATER', 'WT001', '2026-07-23 16:18:05', 'AVAILABLE', 'B201纯水间', '去离子水', NULL, '18.2MΩ·cm 超纯水', 40, '升', '2026-10-23 19:43:05', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (29, 40, 'WATER', 'WT002', '2026-07-23 16:18:05', 'AVAILABLE', 'B201纯水间', '蒸馏水', NULL, '二次蒸馏 实验级', 50, '升', '2026-10-23 19:43:05', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (30, 20, 'WATER', 'WT003', '2026-07-23 16:18:05', 'AVAILABLE', 'B201超纯水机', '超纯水', NULL, 'Milli-Q 18.2MΩ·cm TOC<5ppb', 20, '升', '2026-10-23 19:43:05', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (31, 10, 'WATER', 'WT004', '2026-07-23 16:18:05', 'AVAILABLE', 'B203制剂室', '注射用水', NULL, '药典级 WFI', 10, '升', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 1);
INSERT INTO `t_material` VALUES (32, 12, 'REAGENT', 'RG001', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜', '氢氧化钠', NULL, '分析纯 AR 500g', 12, '瓶', '2026-10-23 19:43:05', 2, b'0', b'1', 'B201 危化品柜 A-2', '64-17-5', b'1', 0, b'0', NULL, 30, '2027-04-23', 1);
INSERT INTO `t_material` VALUES (33, 15, 'REAGENT', 'RG002', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜（酸）', '盐酸', NULL, '分析纯 AR 36%-38% 500ml', 15, '瓶', '2026-10-23 19:43:05', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 1);
INSERT INTO `t_material` VALUES (34, 10, 'REAGENT', 'RG003', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜（酸）', '硫酸', NULL, '分析纯 AR 95%-98% 500ml', 10, '瓶', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 1);
INSERT INTO `t_material` VALUES (35, 8, 'REAGENT', 'RG004', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜（易燃）', '丙酮', NULL, '分析纯 AR 500ml', 8, '瓶', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 1);
INSERT INTO `t_material` VALUES (36, 10, 'REAGENT', 'RG005', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜', '无水碳酸钠', NULL, '基准试剂 250g', 10, '瓶', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 1);
INSERT INTO `t_material` VALUES (37, 15, 'REAGENT', 'RG006', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜', '氯化钠', NULL, '分析纯 AR 500g', 15, '瓶', '2026-10-23 19:43:05', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 1);
INSERT INTO `t_material` VALUES (38, 6, 'REAGENT', 'RG007', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜', '硫酸铜', NULL, '分析纯 AR 500g', 8, '瓶', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 1);
INSERT INTO `t_material` VALUES (39, 12, 'REAGENT', 'RG008', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜', '酚酞指示剂', NULL, '0.5% 乙醇溶液 100ml', 12, '瓶', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 1);
INSERT INTO `t_material` VALUES (40, 10, 'REAGENT', 'RG009', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜', '甲基橙指示剂', NULL, '0.1% 水溶液 100ml', 10, '瓶', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 1);
INSERT INTO `t_material` VALUES (41, 8, 'REAGENT', 'RG010', '2026-07-23 16:18:05', 'AVAILABLE', 'B201化学试剂柜', 'EDTA二钠盐', NULL, '分析纯 AR 250g', 8, '瓶', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 1);
INSERT INTO `t_material` VALUES (42, 40, 'GLASSWARE', 'GW001', '2026-07-23 16:18:05', 'AVAILABLE', 'B201玻璃器皿柜', '烧杯', NULL, '硼硅玻璃 250ml', 40, '个', '2026-10-23 19:43:05', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 1);
INSERT INTO `t_material` VALUES (43, 22, 'GLASSWARE', 'GW002', '2026-07-23 16:18:05', 'AVAILABLE', 'B201玻璃器皿柜', '烧杯', NULL, '硼硅玻璃 500ml', 30, '个', '2026-10-23 19:43:05', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 1);
INSERT INTO `t_material` VALUES (44, 16, 'GLASSWARE', 'GW003', '2026-07-23 16:18:05', 'AVAILABLE', 'B201玻璃器皿柜', '量筒', NULL, '玻璃 100ml', 20, '个', '2026-10-23 19:43:05', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 1);
INSERT INTO `t_material` VALUES (45, 15, 'GLASSWARE', 'GW004', '2026-07-23 16:18:05', 'AVAILABLE', 'B201玻璃器皿柜', '容量瓶', NULL, '棕色 250ml', 15, '个', '2026-10-23 19:43:05', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 1);
INSERT INTO `t_material` VALUES (46, 25, 'GLASSWARE', 'GW005', '2026-07-23 16:18:05', 'AVAILABLE', 'B201玻璃器皿柜', '锥形瓶', NULL, '硼硅玻璃 250ml', 25, '个', '2026-10-23 19:43:05', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 1);
INSERT INTO `t_material` VALUES (47, 8, 'GLASSWARE', 'GW006', '2026-07-23 16:18:05', 'AVAILABLE', 'B201玻璃器皿柜', '滴定管', NULL, '酸式 50ml 0.1精度', 8, '支', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 1);
INSERT INTO `t_material` VALUES (48, 11, 'GLASSWARE', 'GW007', '2026-07-23 16:18:05', 'AVAILABLE', 'B201玻璃器皿柜', '移液管', NULL, '大肚型 10ml A级', 12, '支', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 1);
INSERT INTO `t_material` VALUES (49, 100, 'GLASSWARE', 'GW008', '2026-07-23 16:18:05', 'AVAILABLE', 'B201玻璃器皿柜', '试管', NULL, '玻璃 20×200mm', 100, '支', '2026-10-23 19:43:05', 20, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 1);
INSERT INTO `t_material` VALUES (50, 15, 'GLASSWARE', 'GW009', '2026-07-23 16:18:05', 'AVAILABLE', 'B201玻璃器皿柜', '玻璃漏斗', NULL, '标准 60mm', 15, '个', '2026-10-23 19:43:05', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 1);
INSERT INTO `t_material` VALUES (51, 5, 'GLASSWARE', 'GW010', '2026-07-23 16:18:05', 'AVAILABLE', 'B201玻璃器皿柜', '干燥器', NULL, '真空干燥器 240mm', 5, '个', '2026-10-23 19:43:05', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 1);
INSERT INTO `t_material` VALUES (52, 2, 'DEVICE', 'EQ001', '2026-10-22 23:45:13', 'AVAILABLE', 'A102电子实验室', '数字示波器', NULL, '普源 DS1054Z 50MHz', 5, '台', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (53, 7, 'DEVICE', 'EQ002', '2026-10-22 23:45:13', 'AVAILABLE', 'A102电子实验室', '直流稳压电源', NULL, '普源 DP832A 三通道', 8, '台', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (54, 12, 'DEVICE', 'EQ003', '2026-10-22 23:45:13', 'AVAILABLE', 'A101基础实验室', '数字万用表', NULL, '福禄克 Fluke 17B+', 16, '个', '2026-10-22 23:45:13', 3, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (55, 4, 'DEVICE', 'EQ004', '2026-10-22 23:45:13', 'AVAILABLE', 'A102电子实验室', '信号发生器', NULL, '普源 DG1022Z 25MHz', 4, '台', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (56, 3, 'DEVICE', 'EQ005', '2026-10-22 23:45:13', 'AVAILABLE', 'A103焊接实训室', '防静电焊接台', NULL, '白光 BK881 焊台', 6, '台', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (57, 3, 'DEVICE', 'EQ006', '2026-10-22 23:45:13', 'AVAILABLE', 'A102电子实验室', '逻辑分析仪', NULL, '梦源 LA1010 16通道', 3, '台', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (58, 5, 'DEVICE', 'EQ007', '2026-10-22 23:45:13', 'AVAILABLE', 'A103焊接实训室', '热风枪', NULL, '快克 850A+', 5, '把', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (59, 2, 'DEVICE', 'EQ008', '2026-10-22 23:45:13', 'AVAILABLE', 'A104通信实验室', '频谱分析仪', NULL, '安捷伦 N9000A 3GHz', 2, '台', '2026-10-22 23:45:13', 2, b'1', b'1', NULL, NULL, b'0', 20, b'1', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (60, 3, 'DEVICE', 'EQ009', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学实验室', '电子天平', NULL, '梅特勒 ME204 220g/0.1mg', 3, '台', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (61, 2, 'DEVICE', 'EQ010', '2026-10-22 23:45:13', 'AVAILABLE', 'B202生物实验室', '离心机', NULL, '湘仪 H1650 16500rpm', 2, '台', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (62, 4, 'DEVICE', 'EQ011', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学实验室', 'pH计', NULL, '梅特勒 FE28 0.01精度', 4, '台', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (63, 6, 'DEVICE', 'EQ012', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学实验室', '磁力搅拌器', NULL, 'IKA C-MAG 5L', 6, '台', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-22', 50, NULL, 0);
INSERT INTO `t_material` VALUES (64, 14, 'CONSUMABLE', 'CS001', '2026-10-22 23:45:13', 'AVAILABLE', 'A101耗材柜', 'U盘 32GB', NULL, '闪迪 CZ48 32GB USB3.0', 20, '个', '2026-10-22 23:45:13', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (65, 26, 'CONSUMABLE', 'CS002', '2026-10-22 23:45:13', 'AVAILABLE', 'A101耗材柜', '透明胶带', NULL, '3M 18mm宽 透明', 30, '卷', '2026-10-22 23:45:13', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (66, 50, 'CONSUMABLE', 'CS003', '2026-10-22 23:45:13', 'AVAILABLE', 'A103耗材柜', '杜邦线', NULL, '公母头 20cm 40芯', 50, '排', '2026-10-22 23:45:13', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (67, 15, 'CONSUMABLE', 'CS004', '2026-10-22 23:45:13', 'AVAILABLE', 'A103耗材柜', '焊锡丝', NULL, '千住 0.8mm 含松香 1kg', 15, '卷', '2026-10-22 23:45:13', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (68, 20, 'CONSUMABLE', 'CS005', '2026-10-22 23:45:13', 'AVAILABLE', 'A103耗材柜', '热缩管套装', NULL, '2-10mm 各规格 600mm长', 25, '套', '2026-10-22 23:45:13', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (69, 50, 'CONSUMABLE', 'CS006', '2026-10-22 23:45:13', 'AVAILABLE', 'A101耗材柜', '实验记录本', NULL, 'A4 80页 方格内页', 50, '本', '2026-10-22 23:45:13', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (70, 200, 'CONSUMABLE', 'CS007', '2026-10-22 23:45:13', 'AVAILABLE', 'A101耗材柜', '标签纸', NULL, '60×40mm 白色不干胶', 200, '张', '2026-10-22 23:45:13', 40, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (71, 36, 'CONSUMABLE', 'CS008', '2026-10-22 23:45:13', 'AVAILABLE', 'A103耗材柜', '尼龙扎带', NULL, '3×100mm 黑色 100根/包', 40, '包', '2026-10-22 23:45:13', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (72, 20, 'CONSUMABLE', 'CS009', '2026-10-22 23:45:13', 'AVAILABLE', 'A103耗材柜', '松香助焊剂', NULL, '固体松香 50g', 20, '盒', '2026-10-22 23:45:13', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (73, 30, 'CONSUMABLE', 'CS010', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜', '无水乙醇', NULL, '分析纯 500ml', 30, '瓶', '2026-10-22 23:45:13', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (74, 25, 'CONSUMABLE', 'CS011', '2026-10-22 23:45:13', 'AVAILABLE', 'B201耗材柜', '脱脂棉', NULL, '医用级 100g/包', 25, '包', '2026-10-22 23:45:13', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (75, 20, 'CONSUMABLE', 'CS012', '2026-10-22 23:45:13', 'AVAILABLE', 'B201耗材柜', '滤纸', NULL, '定性 Φ9cm 100张/盒', 20, '盒', '2026-10-22 23:45:13', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (76, 15, 'CONSUMABLE', 'CS013', '2026-10-22 23:45:13', 'AVAILABLE', 'B202防护用品柜', '乳胶手套', NULL, '一次性 无粉 M码 100只', 15, '盒', '2026-10-22 23:45:13', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (77, 20, 'CONSUMABLE', 'CS014', '2026-10-22 23:45:13', 'AVAILABLE', 'B202防护用品柜', '口罩', NULL, '一次性医用 50只/盒', 20, '盒', '2026-10-22 23:45:13', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (78, 30, 'CONSUMABLE', 'CS015', '2026-10-22 23:45:13', 'AVAILABLE', 'A101耗材柜', '电池 AA', NULL, '南孚 5号 1.5V 4粒装', 30, '组', '2026-10-22 23:45:13', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (79, 34, 'WATER', 'WT001', '2026-10-22 23:45:13', 'AVAILABLE', 'B201纯水间', '去离子水', NULL, '18.2MΩ·cm 超纯水', 40, '升', '2026-10-22 23:45:13', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (80, 40, 'WATER', 'WT002', '2026-10-22 23:45:13', 'AVAILABLE', 'B201纯水间', '蒸馏水', NULL, '二次蒸馏 实验级', 50, '升', '2026-10-22 23:45:13', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (81, 20, 'WATER', 'WT003', '2026-10-22 23:45:13', 'AVAILABLE', 'B201超纯水机', '超纯水', NULL, 'Milli-Q 18.2MΩ·cm TOC<5ppb', 20, '升', '2026-10-22 23:45:13', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (82, 10, 'WATER', 'WT004', '2026-10-22 23:45:13', 'AVAILABLE', 'B203制剂室', '注射用水', NULL, '药典级 WFI', 10, '升', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (83, 12, 'REAGENT', 'RG001', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜', '氢氧化钠', NULL, '分析纯 AR 500g', 12, '瓶', '2026-10-22 23:45:13', 2, b'0', b'1', 'B201 危化品柜 A-2', '64-17-5', b'1', 0, b'0', NULL, 30, '2027-04-22', 0);
INSERT INTO `t_material` VALUES (84, 15, 'REAGENT', 'RG002', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜（酸）', '盐酸', NULL, '分析纯 AR 36%-38% 500ml', 15, '瓶', '2026-10-22 23:45:13', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-22', 0);
INSERT INTO `t_material` VALUES (85, 10, 'REAGENT', 'RG003', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜（酸）', '硫酸', NULL, '分析纯 AR 95%-98% 500ml', 10, '瓶', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-22', 0);
INSERT INTO `t_material` VALUES (86, 8, 'REAGENT', 'RG004', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜（易燃）', '丙酮', NULL, '分析纯 AR 500ml', 8, '瓶', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-22', 0);
INSERT INTO `t_material` VALUES (87, 10, 'REAGENT', 'RG005', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜', '无水碳酸钠', NULL, '基准试剂 250g', 10, '瓶', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-22', 0);
INSERT INTO `t_material` VALUES (88, 15, 'REAGENT', 'RG006', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜', '氯化钠', NULL, '分析纯 AR 500g', 15, '瓶', '2026-10-22 23:45:13', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-22', 0);
INSERT INTO `t_material` VALUES (89, 6, 'REAGENT', 'RG007', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜', '硫酸铜', NULL, '分析纯 AR 500g', 8, '瓶', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-22', 0);
INSERT INTO `t_material` VALUES (90, 12, 'REAGENT', 'RG008', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜', '酚酞指示剂', NULL, '0.5% 乙醇溶液 100ml', 12, '瓶', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-22', 0);
INSERT INTO `t_material` VALUES (91, 10, 'REAGENT', 'RG009', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜', '甲基橙指示剂', NULL, '0.1% 水溶液 100ml', 10, '瓶', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-22', 0);
INSERT INTO `t_material` VALUES (92, 8, 'REAGENT', 'RG010', '2026-10-22 23:45:13', 'AVAILABLE', 'B201化学试剂柜', 'EDTA二钠盐', NULL, '分析纯 AR 250g', 8, '瓶', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-22', 0);
INSERT INTO `t_material` VALUES (93, 40, 'GLASSWARE', 'GW001', '2026-10-22 23:45:13', 'AVAILABLE', 'B201玻璃器皿柜', '烧杯', NULL, '硼硅玻璃 250ml', 40, '个', '2026-10-22 23:45:13', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-22', 2, NULL, 0);
INSERT INTO `t_material` VALUES (94, 22, 'GLASSWARE', 'GW002', '2026-10-22 23:45:13', 'AVAILABLE', 'B201玻璃器皿柜', '烧杯', NULL, '硼硅玻璃 500ml', 30, '个', '2026-10-22 23:45:13', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-22', 2, NULL, 0);
INSERT INTO `t_material` VALUES (95, 16, 'GLASSWARE', 'GW003', '2026-10-22 23:45:13', 'AVAILABLE', 'B201玻璃器皿柜', '量筒', NULL, '玻璃 100ml', 20, '个', '2026-10-22 23:45:13', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-22', 2, NULL, 0);
INSERT INTO `t_material` VALUES (96, 15, 'GLASSWARE', 'GW004', '2026-10-22 23:45:13', 'AVAILABLE', 'B201玻璃器皿柜', '容量瓶', NULL, '棕色 250ml', 15, '个', '2026-10-22 23:45:13', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-22', 2, NULL, 0);
INSERT INTO `t_material` VALUES (97, 25, 'GLASSWARE', 'GW005', '2026-10-22 23:45:13', 'AVAILABLE', 'B201玻璃器皿柜', '锥形瓶', NULL, '硼硅玻璃 250ml', 25, '个', '2026-10-22 23:45:13', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-22', 2, NULL, 0);
INSERT INTO `t_material` VALUES (98, 8, 'GLASSWARE', 'GW006', '2026-10-22 23:45:13', 'AVAILABLE', 'B201玻璃器皿柜', '滴定管', NULL, '酸式 50ml 0.1精度', 8, '支', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-22', 2, NULL, 0);
INSERT INTO `t_material` VALUES (99, 11, 'GLASSWARE', 'GW007', '2026-10-22 23:45:13', 'AVAILABLE', 'B201玻璃器皿柜', '移液管', NULL, '大肚型 10ml A级', 12, '支', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-22', 2, NULL, 0);
INSERT INTO `t_material` VALUES (100, 100, 'GLASSWARE', 'GW008', '2026-10-22 23:45:13', 'AVAILABLE', 'B201玻璃器皿柜', '试管', NULL, '玻璃 20×200mm', 100, '支', '2026-10-22 23:45:13', 20, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-22', 2, NULL, 0);
INSERT INTO `t_material` VALUES (101, 15, 'GLASSWARE', 'GW009', '2026-10-22 23:45:13', 'AVAILABLE', 'B201玻璃器皿柜', '玻璃漏斗', NULL, '标准 60mm', 15, '个', '2026-10-22 23:45:13', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-22', 2, NULL, 0);
INSERT INTO `t_material` VALUES (102, 5, 'GLASSWARE', 'GW010', '2026-10-22 23:45:13', 'AVAILABLE', 'B201玻璃器皿柜', '干燥器', NULL, '真空干燥器 240mm', 5, '个', '2026-10-22 23:45:13', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-22', 2, NULL, 0);
INSERT INTO `t_material` VALUES (103, 2, 'DEVICE', 'EQ001', '2026-10-23 19:16:22', 'AVAILABLE', 'A102电子实验室', '数字示波器', NULL, '普源 DS1054Z 50MHz', 5, '台', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (104, 7, 'DEVICE', 'EQ002', '2026-10-23 19:16:22', 'AVAILABLE', 'A102电子实验室', '直流稳压电源', NULL, '普源 DP832A 三通道', 8, '台', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (105, 12, 'DEVICE', 'EQ003', '2026-10-23 19:16:22', 'AVAILABLE', 'A101基础实验室', '数字万用表', NULL, '福禄克 Fluke 17B+', 16, '个', '2026-10-23 19:16:22', 3, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (106, 4, 'DEVICE', 'EQ004', '2026-10-23 19:16:22', 'AVAILABLE', 'A102电子实验室', '信号发生器', NULL, '普源 DG1022Z 25MHz', 4, '台', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (107, 3, 'DEVICE', 'EQ005', '2026-10-23 19:16:22', 'AVAILABLE', 'A103焊接实训室', '防静电焊接台', NULL, '白光 BK881 焊台', 6, '台', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (108, 3, 'DEVICE', 'EQ006', '2026-10-23 19:16:22', 'AVAILABLE', 'A102电子实验室', '逻辑分析仪', NULL, '梦源 LA1010 16通道', 3, '台', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (109, 5, 'DEVICE', 'EQ007', '2026-10-23 19:16:22', 'AVAILABLE', 'A103焊接实训室', '热风枪', NULL, '快克 850A+', 5, '把', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (110, 2, 'DEVICE', 'EQ008', '2026-10-23 19:16:22', 'AVAILABLE', 'A104通信实验室', '频谱分析仪', NULL, '安捷伦 N9000A 3GHz', 2, '台', '2026-10-23 19:38:10', 2, b'1', b'1', NULL, NULL, b'0', 20, b'1', '2027-04-23', 50, NULL, 1);
INSERT INTO `t_material` VALUES (111, 3, 'DEVICE', 'EQ009', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学实验室', '电子天平', NULL, '梅特勒 ME204 220g/0.1mg', 3, '台', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (112, 2, 'DEVICE', 'EQ010', '2026-10-23 19:16:22', 'AVAILABLE', 'B202生物实验室', '离心机', NULL, '湘仪 H1650 16500rpm', 2, '台', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (113, 4, 'DEVICE', 'EQ011', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学实验室', 'pH计', NULL, '梅特勒 FE28 0.01精度', 4, '台', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (114, 6, 'DEVICE', 'EQ012', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学实验室', '磁力搅拌器', NULL, 'IKA C-MAG 5L', 6, '台', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (115, 14, 'CONSUMABLE', 'CS001', '2026-10-23 19:16:22', 'AVAILABLE', 'A101耗材柜', 'U盘 32GB', NULL, '闪迪 CZ48 32GB USB3.0', 20, '个', '2026-10-23 19:16:22', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (116, 26, 'CONSUMABLE', 'CS002', '2026-10-23 19:16:22', 'AVAILABLE', 'A101耗材柜', '透明胶带', NULL, '3M 18mm宽 透明', 30, '卷', '2026-10-23 19:16:22', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (117, 50, 'CONSUMABLE', 'CS003', '2026-10-23 19:16:22', 'AVAILABLE', 'A103耗材柜', '杜邦线', NULL, '公母头 20cm 40芯', 50, '排', '2026-10-23 19:16:22', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (118, 15, 'CONSUMABLE', 'CS004', '2026-10-23 19:16:22', 'AVAILABLE', 'A103耗材柜', '焊锡丝', NULL, '千住 0.8mm 含松香 1kg', 15, '卷', '2026-10-23 19:16:22', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (119, 20, 'CONSUMABLE', 'CS005', '2026-10-23 19:16:22', 'AVAILABLE', 'A103耗材柜', '热缩管套装', NULL, '2-10mm 各规格 600mm长', 25, '套', '2026-10-23 19:16:22', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (120, 50, 'CONSUMABLE', 'CS006', '2026-10-23 19:16:22', 'AVAILABLE', 'A101耗材柜', '实验记录本', NULL, 'A4 80页 方格内页', 50, '本', '2026-10-23 19:16:22', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (121, 200, 'CONSUMABLE', 'CS007', '2026-10-23 19:16:22', 'AVAILABLE', 'A101耗材柜', '标签纸', NULL, '60×40mm 白色不干胶', 200, '张', '2026-10-23 19:16:22', 40, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (122, 36, 'CONSUMABLE', 'CS008', '2026-10-23 19:16:22', 'AVAILABLE', 'A103耗材柜', '尼龙扎带', NULL, '3×100mm 黑色 100根/包', 40, '包', '2026-10-23 19:16:22', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (123, 20, 'CONSUMABLE', 'CS009', '2026-10-23 19:16:22', 'AVAILABLE', 'A103耗材柜', '松香助焊剂', NULL, '固体松香 50g', 20, '盒', '2026-10-23 19:16:22', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (124, 30, 'CONSUMABLE', 'CS010', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜', '无水乙醇', NULL, '分析纯 500ml', 30, '瓶', '2026-10-23 19:16:22', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (125, 25, 'CONSUMABLE', 'CS011', '2026-10-23 19:16:22', 'AVAILABLE', 'B201耗材柜', '脱脂棉', NULL, '医用级 100g/包', 25, '包', '2026-10-23 19:16:22', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (126, 20, 'CONSUMABLE', 'CS012', '2026-10-23 19:16:22', 'AVAILABLE', 'B201耗材柜', '滤纸', NULL, '定性 Φ9cm 100张/盒', 20, '盒', '2026-10-23 19:16:22', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (127, 15, 'CONSUMABLE', 'CS013', '2026-10-23 19:16:22', 'AVAILABLE', 'B202防护用品柜', '乳胶手套', NULL, '一次性 无粉 M码 100只', 15, '盒', '2026-10-23 19:16:22', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (128, 20, 'CONSUMABLE', 'CS014', '2026-10-23 19:16:22', 'AVAILABLE', 'B202防护用品柜', '口罩', NULL, '一次性医用 50只/盒', 20, '盒', '2026-10-23 19:16:22', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (129, 30, 'CONSUMABLE', 'CS015', '2026-10-23 19:16:22', 'AVAILABLE', 'A101耗材柜', '电池 AA', NULL, '南孚 5号 1.5V 4粒装', 30, '组', '2026-10-23 19:16:22', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (130, 34, 'WATER', 'WT001', '2026-10-23 19:16:22', 'AVAILABLE', 'B201纯水间', '去离子水', NULL, '18.2MΩ·cm 超纯水', 40, '升', '2026-10-23 19:16:22', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (131, 40, 'WATER', 'WT002', '2026-10-23 19:16:22', 'AVAILABLE', 'B201纯水间', '蒸馏水', NULL, '二次蒸馏 实验级', 50, '升', '2026-10-23 19:16:22', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (132, 20, 'WATER', 'WT003', '2026-10-23 19:16:22', 'AVAILABLE', 'B201超纯水机', '超纯水', NULL, 'Milli-Q 18.2MΩ·cm TOC<5ppb', 20, '升', '2026-10-23 19:16:22', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (133, 10, 'WATER', 'WT004', '2026-10-23 19:16:22', 'AVAILABLE', 'B203制剂室', '注射用水', NULL, '药典级 WFI', 10, '升', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (134, 12, 'REAGENT', 'RG001', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜', '氢氧化钠', NULL, '分析纯 AR 500g', 12, '瓶', '2026-10-23 19:16:22', 2, b'0', b'1', 'B201 危化品柜 A-2', '64-17-5', b'1', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (135, 15, 'REAGENT', 'RG002', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜（酸）', '盐酸', NULL, '分析纯 AR 36%-38% 500ml', 15, '瓶', '2026-10-23 19:16:22', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (136, 10, 'REAGENT', 'RG003', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜（酸）', '硫酸', NULL, '分析纯 AR 95%-98% 500ml', 10, '瓶', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (137, 8, 'REAGENT', 'RG004', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜（易燃）', '丙酮', NULL, '分析纯 AR 500ml', 8, '瓶', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (138, 10, 'REAGENT', 'RG005', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜', '无水碳酸钠', NULL, '基准试剂 250g', 10, '瓶', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (139, 15, 'REAGENT', 'RG006', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜', '氯化钠', NULL, '分析纯 AR 500g', 15, '瓶', '2026-10-23 19:16:22', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (140, 6, 'REAGENT', 'RG007', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜', '硫酸铜', NULL, '分析纯 AR 500g', 8, '瓶', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (141, 12, 'REAGENT', 'RG008', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜', '酚酞指示剂', NULL, '0.5% 乙醇溶液 100ml', 12, '瓶', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (142, 10, 'REAGENT', 'RG009', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜', '甲基橙指示剂', NULL, '0.1% 水溶液 100ml', 10, '瓶', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (143, 8, 'REAGENT', 'RG010', '2026-10-23 19:16:22', 'AVAILABLE', 'B201化学试剂柜', 'EDTA二钠盐', NULL, '分析纯 AR 250g', 8, '瓶', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (144, 40, 'GLASSWARE', 'GW001', '2026-10-23 19:16:22', 'AVAILABLE', 'B201玻璃器皿柜', '烧杯', NULL, '硼硅玻璃 250ml', 40, '个', '2026-10-23 19:16:22', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (145, 22, 'GLASSWARE', 'GW002', '2026-10-23 19:16:22', 'AVAILABLE', 'B201玻璃器皿柜', '烧杯', NULL, '硼硅玻璃 500ml', 30, '个', '2026-10-23 19:16:22', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (146, 16, 'GLASSWARE', 'GW003', '2026-10-23 19:16:22', 'AVAILABLE', 'B201玻璃器皿柜', '量筒', NULL, '玻璃 100ml', 20, '个', '2026-10-23 19:16:22', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (147, 15, 'GLASSWARE', 'GW004', '2026-10-23 19:16:22', 'AVAILABLE', 'B201玻璃器皿柜', '容量瓶', NULL, '棕色 250ml', 15, '个', '2026-10-23 19:16:22', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (148, 25, 'GLASSWARE', 'GW005', '2026-10-23 19:16:22', 'AVAILABLE', 'B201玻璃器皿柜', '锥形瓶', NULL, '硼硅玻璃 250ml', 25, '个', '2026-10-23 19:16:22', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (149, 8, 'GLASSWARE', 'GW006', '2026-10-23 19:16:22', 'AVAILABLE', 'B201玻璃器皿柜', '滴定管', NULL, '酸式 50ml 0.1精度', 8, '支', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (150, 11, 'GLASSWARE', 'GW007', '2026-10-23 19:16:22', 'AVAILABLE', 'B201玻璃器皿柜', '移液管', NULL, '大肚型 10ml A级', 12, '支', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (151, 100, 'GLASSWARE', 'GW008', '2026-10-23 19:16:22', 'AVAILABLE', 'B201玻璃器皿柜', '试管', NULL, '玻璃 20×200mm', 100, '支', '2026-10-23 19:16:22', 20, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (152, 15, 'GLASSWARE', 'GW009', '2026-10-23 19:16:22', 'AVAILABLE', 'B201玻璃器皿柜', '玻璃漏斗', NULL, '标准 60mm', 15, '个', '2026-10-23 19:16:22', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (153, 5, 'GLASSWARE', 'GW010', '2026-10-23 19:16:22', 'AVAILABLE', 'B201玻璃器皿柜', '干燥器', NULL, '真空干燥器 240mm', 5, '个', '2026-10-23 19:16:22', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (154, 2, 'DEVICE', 'EQ001', '2026-10-23 19:38:10', 'AVAILABLE', 'A102电子实验室', '数字示波器', NULL, '普源 DS1054Z 50MHz', 5, '台', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (155, 7, 'DEVICE', 'EQ002', '2026-10-23 19:38:10', 'AVAILABLE', 'A102电子实验室', '直流稳压电源', NULL, '普源 DP832A 三通道', 8, '台', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (156, 12, 'DEVICE', 'EQ003', '2026-10-23 19:38:10', 'AVAILABLE', 'A101基础实验室', '数字万用表', NULL, '福禄克 Fluke 17B+', 16, '个', '2026-10-23 19:38:10', 3, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (157, 4, 'DEVICE', 'EQ004', '2026-10-23 19:38:10', 'AVAILABLE', 'A102电子实验室', '信号发生器', NULL, '普源 DG1022Z 25MHz', 4, '台', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (158, 3, 'DEVICE', 'EQ005', '2026-10-23 19:38:10', 'AVAILABLE', 'A103焊接实训室', '防静电焊接台', NULL, '白光 BK881 焊台', 6, '台', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (159, 3, 'DEVICE', 'EQ006', '2026-10-23 19:38:10', 'AVAILABLE', 'A102电子实验室', '逻辑分析仪', NULL, '梦源 LA1010 16通道', 3, '台', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (160, 5, 'DEVICE', 'EQ007', '2026-10-23 19:38:10', 'AVAILABLE', 'A103焊接实训室', '热风枪', NULL, '快克 850A+', 5, '把', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (161, 2, 'DEVICE', 'EQ008', '2026-10-23 19:38:10', 'AVAILABLE', 'A104通信实验室', '频谱分析仪', NULL, '安捷伦 N9000A 3GHz', 2, '台', '2026-10-23 19:43:05', 2, b'1', b'1', NULL, NULL, b'0', 20, b'1', '2027-04-23', 50, NULL, 1);
INSERT INTO `t_material` VALUES (162, 3, 'DEVICE', 'EQ009', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学实验室', '电子天平', NULL, '梅特勒 ME204 220g/0.1mg', 3, '台', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (163, 2, 'DEVICE', 'EQ010', '2026-10-23 19:38:10', 'AVAILABLE', 'B202生物实验室', '离心机', NULL, '湘仪 H1650 16500rpm', 2, '台', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (164, 4, 'DEVICE', 'EQ011', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学实验室', 'pH计', NULL, '梅特勒 FE28 0.01精度', 4, '台', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (165, 6, 'DEVICE', 'EQ012', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学实验室', '磁力搅拌器', NULL, 'IKA C-MAG 5L', 6, '台', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 20, b'0', '2027-04-23', 50, NULL, 0);
INSERT INTO `t_material` VALUES (166, 14, 'CONSUMABLE', 'CS001', '2026-10-23 19:38:10', 'AVAILABLE', 'A101耗材柜', 'U盘 32GB', NULL, '闪迪 CZ48 32GB USB3.0', 20, '个', '2026-10-23 19:38:10', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (167, 26, 'CONSUMABLE', 'CS002', '2026-10-23 19:38:10', 'AVAILABLE', 'A101耗材柜', '透明胶带', NULL, '3M 18mm宽 透明', 30, '卷', '2026-10-23 19:38:10', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (168, 50, 'CONSUMABLE', 'CS003', '2026-10-23 19:38:10', 'AVAILABLE', 'A103耗材柜', '杜邦线', NULL, '公母头 20cm 40芯', 50, '排', '2026-10-23 19:38:10', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (169, 15, 'CONSUMABLE', 'CS004', '2026-10-23 19:38:10', 'AVAILABLE', 'A103耗材柜', '焊锡丝', NULL, '千住 0.8mm 含松香 1kg', 15, '卷', '2026-10-23 19:38:10', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (170, 20, 'CONSUMABLE', 'CS005', '2026-10-23 19:38:10', 'AVAILABLE', 'A103耗材柜', '热缩管套装', NULL, '2-10mm 各规格 600mm长', 25, '套', '2026-10-23 19:38:10', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (171, 50, 'CONSUMABLE', 'CS006', '2026-10-23 19:38:10', 'AVAILABLE', 'A101耗材柜', '实验记录本', NULL, 'A4 80页 方格内页', 50, '本', '2026-10-23 19:38:10', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (172, 200, 'CONSUMABLE', 'CS007', '2026-10-23 19:38:10', 'AVAILABLE', 'A101耗材柜', '标签纸', NULL, '60×40mm 白色不干胶', 200, '张', '2026-10-23 19:38:10', 40, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (173, 36, 'CONSUMABLE', 'CS008', '2026-10-23 19:38:10', 'AVAILABLE', 'A103耗材柜', '尼龙扎带', NULL, '3×100mm 黑色 100根/包', 40, '包', '2026-10-23 19:38:10', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (174, 20, 'CONSUMABLE', 'CS009', '2026-10-23 19:38:10', 'AVAILABLE', 'A103耗材柜', '松香助焊剂', NULL, '固体松香 50g', 20, '盒', '2026-10-23 19:38:10', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (175, 30, 'CONSUMABLE', 'CS010', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜', '无水乙醇', NULL, '分析纯 500ml', 30, '瓶', '2026-10-23 19:38:10', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (176, 25, 'CONSUMABLE', 'CS011', '2026-10-23 19:38:10', 'AVAILABLE', 'B201耗材柜', '脱脂棉', NULL, '医用级 100g/包', 25, '包', '2026-10-23 19:38:10', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (177, 20, 'CONSUMABLE', 'CS012', '2026-10-23 19:38:10', 'AVAILABLE', 'B201耗材柜', '滤纸', NULL, '定性 Φ9cm 100张/盒', 20, '盒', '2026-10-23 19:38:10', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (178, 15, 'CONSUMABLE', 'CS013', '2026-10-23 19:38:10', 'AVAILABLE', 'B202防护用品柜', '乳胶手套', NULL, '一次性 无粉 M码 100只', 15, '盒', '2026-10-23 19:38:10', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (179, 20, 'CONSUMABLE', 'CS014', '2026-10-23 19:38:10', 'AVAILABLE', 'B202防护用品柜', '口罩', NULL, '一次性医用 50只/盒', 20, '盒', '2026-10-23 19:38:10', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (180, 30, 'CONSUMABLE', 'CS015', '2026-10-23 19:38:10', 'AVAILABLE', 'A101耗材柜', '电池 AA', NULL, '南孚 5号 1.5V 4粒装', 30, '组', '2026-10-23 19:38:10', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (181, 34, 'WATER', 'WT001', '2026-10-23 19:38:10', 'AVAILABLE', 'B201纯水间', '去离子水', NULL, '18.2MΩ·cm 超纯水', 40, '升', '2026-10-23 19:38:10', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (182, 40, 'WATER', 'WT002', '2026-10-23 19:38:10', 'AVAILABLE', 'B201纯水间', '蒸馏水', NULL, '二次蒸馏 实验级', 50, '升', '2026-10-23 19:38:10', 10, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (183, 20, 'WATER', 'WT003', '2026-10-23 19:38:10', 'AVAILABLE', 'B201超纯水机', '超纯水', NULL, 'Milli-Q 18.2MΩ·cm TOC<5ppb', 20, '升', '2026-10-23 19:38:10', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (184, 10, 'WATER', 'WT004', '2026-10-23 19:38:10', 'AVAILABLE', 'B203制剂室', '注射用水', NULL, '药典级 WFI', 10, '升', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 2, NULL, 0);
INSERT INTO `t_material` VALUES (185, 12, 'REAGENT', 'RG001', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜', '氢氧化钠', NULL, '分析纯 AR 500g', 12, '瓶', '2026-10-23 19:38:10', 2, b'0', b'1', 'B201 危化品柜 A-2', '64-17-5', b'1', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (186, 15, 'REAGENT', 'RG002', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜（酸）', '盐酸', NULL, '分析纯 AR 36%-38% 500ml', 15, '瓶', '2026-10-23 19:38:10', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (187, 10, 'REAGENT', 'RG003', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜（酸）', '硫酸', NULL, '分析纯 AR 95%-98% 500ml', 10, '瓶', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (188, 8, 'REAGENT', 'RG004', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜（易燃）', '丙酮', NULL, '分析纯 AR 500ml', 8, '瓶', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (189, 10, 'REAGENT', 'RG005', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜', '无水碳酸钠', NULL, '基准试剂 250g', 10, '瓶', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (190, 15, 'REAGENT', 'RG006', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜', '氯化钠', NULL, '分析纯 AR 500g', 15, '瓶', '2026-10-23 19:38:10', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (191, 6, 'REAGENT', 'RG007', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜', '硫酸铜', NULL, '分析纯 AR 500g', 8, '瓶', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (192, 12, 'REAGENT', 'RG008', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜', '酚酞指示剂', NULL, '0.5% 乙醇溶液 100ml', 12, '瓶', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (193, 10, 'REAGENT', 'RG009', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜', '甲基橙指示剂', NULL, '0.1% 水溶液 100ml', 10, '瓶', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (194, 8, 'REAGENT', 'RG010', '2026-10-23 19:38:10', 'AVAILABLE', 'B201化学试剂柜', 'EDTA二钠盐', NULL, '分析纯 AR 250g', 8, '瓶', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 30, '2027-04-23', 0);
INSERT INTO `t_material` VALUES (195, 40, 'GLASSWARE', 'GW001', '2026-10-23 19:38:10', 'AVAILABLE', 'B201玻璃器皿柜', '烧杯', NULL, '硼硅玻璃 250ml', 40, '个', '2026-10-23 19:38:10', 8, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (196, 22, 'GLASSWARE', 'GW002', '2026-10-23 19:38:10', 'AVAILABLE', 'B201玻璃器皿柜', '烧杯', NULL, '硼硅玻璃 500ml', 30, '个', '2026-10-23 19:38:10', 6, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (197, 16, 'GLASSWARE', 'GW003', '2026-10-23 19:38:10', 'AVAILABLE', 'B201玻璃器皿柜', '量筒', NULL, '玻璃 100ml', 20, '个', '2026-10-23 19:38:10', 4, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (198, 15, 'GLASSWARE', 'GW004', '2026-10-23 19:38:10', 'AVAILABLE', 'B201玻璃器皿柜', '容量瓶', NULL, '棕色 250ml', 15, '个', '2026-10-23 19:38:10', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (199, 25, 'GLASSWARE', 'GW005', '2026-10-23 19:38:10', 'AVAILABLE', 'B201玻璃器皿柜', '锥形瓶', NULL, '硼硅玻璃 250ml', 25, '个', '2026-10-23 19:38:10', 5, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (200, 8, 'GLASSWARE', 'GW006', '2026-10-23 19:38:10', 'AVAILABLE', 'B201玻璃器皿柜', '滴定管', NULL, '酸式 50ml 0.1精度', 8, '支', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (201, 11, 'GLASSWARE', 'GW007', '2026-10-23 19:38:10', 'AVAILABLE', 'B201玻璃器皿柜', '移液管', NULL, '大肚型 10ml A级', 12, '支', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (202, 100, 'GLASSWARE', 'GW008', '2026-10-23 19:38:10', 'AVAILABLE', 'B201玻璃器皿柜', '试管', NULL, '玻璃 20×200mm', 100, '支', '2026-10-23 19:38:10', 20, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (203, 15, 'GLASSWARE', 'GW009', '2026-10-23 19:38:10', 'AVAILABLE', 'B201玻璃器皿柜', '玻璃漏斗', NULL, '标准 60mm', 15, '个', '2026-10-23 19:38:10', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (204, 5, 'GLASSWARE', 'GW010', '2026-10-23 19:38:10', 'AVAILABLE', 'B201玻璃器皿柜', '干燥器', NULL, '真空干燥器 240mm', 5, '个', '2026-10-23 19:38:10', 2, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 2, NULL, 0);
INSERT INTO `t_material` VALUES (207, 2, 'DEVICE', 'M1792755948135', '2026-10-23 19:45:48', 'AVAILABLE', 'A102电子实验室', '数字示波器', NULL, 'DS1054Z 50MHz', 2, '台', '2026-10-23 19:47:43', 1, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 0, NULL, 3);
INSERT INTO `t_material` VALUES (208, 20, 'CONSUMABLE', 'M1792755948166', '2026-10-23 19:45:48', 'AVAILABLE', 'A103耗材柜', '杜邦线', NULL, '公母头 20cm 40芯', 20, '排', '2026-10-23 19:45:48', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 0, NULL, 0);
INSERT INTO `t_material` VALUES (209, 2, 'DEVICE', 'M1792756075420', '2026-10-23 19:47:55', 'AVAILABLE', 'A102电子实验室', '数字示波器', NULL, 'DS1054Z 50MHz', 2, '台', '2026-10-23 19:48:51', 1, b'0', b'1', NULL, NULL, b'0', 0, b'0', '2027-04-23', 0, NULL, 1);
INSERT INTO `t_material` VALUES (210, 20, 'CONSUMABLE', 'M1792756075432', '2026-10-23 19:47:55', 'AVAILABLE', 'A103耗材柜', '杜邦线', NULL, '公母头 20cm 40芯', 20, '排', '2026-10-23 19:47:55', 3, b'0', b'1', NULL, NULL, b'0', 0, b'0', NULL, 0, NULL, 0);

-- ----------------------------
-- Table structure for t_notification
-- ----------------------------
DROP TABLE IF EXISTS `t_notification`;
CREATE TABLE `t_notification`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 93 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_notification
-- ----------------------------
INSERT INTO `t_notification` VALUES (1, '您借用的「数字万用表」已于 2026-08-05 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '李华');
INSERT INTO `t_notification` VALUES (2, '您借用的「热缩管套装」已于 2026-08-05 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '张明');
INSERT INTO `t_notification` VALUES (3, '您借用的「硫酸铜」已于 2026-08-05 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '赵强');
INSERT INTO `t_notification` VALUES (4, '您借用的「移液管」已于 2026-08-05 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '张明');
INSERT INTO `t_notification` VALUES (5, '您借用的「数字示波器」已于 2026-08-04 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '张明');
INSERT INTO `t_notification` VALUES (6, '您借用的「去离子水」已于 2026-08-04 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '王芳');
INSERT INTO `t_notification` VALUES (7, '您借用的「烧杯」已于 2026-08-04 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '刘洋');
INSERT INTO `t_notification` VALUES (8, '您借用的「U盘 32GB」已于 2026-08-03 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '王芳');
INSERT INTO `t_notification` VALUES (9, '您借用的「蒸馏水」已于 2026-08-03 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '李华');
INSERT INTO `t_notification` VALUES (10, '您借用的「尼龙扎带」已于 2026-08-02 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '刘洋');
INSERT INTO `t_notification` VALUES (11, '您借用的「透明胶带」已于 2026-08-01 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '赵强');
INSERT INTO `t_notification` VALUES (12, '您借用的「量筒」已于 2026-07-31 到期，请尽快归还。', '2026-08-07 17:07:29', 0, '物资逾期提醒', 'OVERDUE', '王芳');
INSERT INTO `t_notification` VALUES (13, '您预约的「热风枪」（2026-09-17 19:00 ~ 2026-09-17 21:00）已提交，等待教师审批。', '2026-09-17 17:27:59', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (14, '张明 申请预约「热风枪」（2026-09-17 19:00 ~ 2026-09-17 21:00），请及时审批。', '2026-09-17 17:27:59', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (15, '您预约的「热风枪」（2026-09-17 19:00 ~ 2026-09-17 21:00）已批准，已生成借用单，请使用后按时归还。', '2026-09-17 20:04:12', 0, '预约申请已通过', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (16, '您借用的「热风枪」已于 2026-09-17 到期，请尽快归还。', '2026-10-11 22:08:43', 0, '物资逾期提醒', 'OVERDUE', 'zhangming');
INSERT INTO `t_notification` VALUES (17, '您预约的「直流稳压电源」（2026-10-12 07:30 ~ 2026-10-12 10:30）已提交，等待教师审批。', '2026-10-11 22:15:19', 0, '预约申请已提交', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (18, '王老师 申请预约「直流稳压电源」（2026-10-12 07:30 ~ 2026-10-12 10:30），请及时审批。', '2026-10-11 22:15:19', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (19, '您预约的「直流稳压电源」（2026-10-12 07:30 ~ 2026-10-12 10:30）已批准，已生成借用单，请使用后按时归还。', '2026-10-11 22:16:13', 0, '预约申请已通过', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (20, '您预约的「数字示波器」（2026-10-12 07:30 ~ 2026-10-12 10:00）已提交，等待教师审批。', '2026-10-11 22:16:50', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (21, '张明 申请预约「数字示波器」（2026-10-12 07:30 ~ 2026-10-12 10:00），请及时审批。', '2026-10-11 22:16:50', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (22, '您预约的「逻辑分析仪」（2026-10-13 12:00 ~ 2026-10-13 15:00）已提交，等待教师审批。', '2026-10-11 22:26:01', 1, '预约申请已提交', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (23, '李华 申请预约「逻辑分析仪」（2026-10-13 12:00 ~ 2026-10-13 15:00），请及时审批。', '2026-10-11 22:26:01', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (24, '您借用的「直流稳压电源」已于 2026-10-12 到期，请尽快归还。', '2026-10-12 19:41:12', 0, '物资逾期提醒', 'OVERDUE', 'teacher');
INSERT INTO `t_notification` VALUES (25, '您预约的「数字示波器」（2026-10-12 07:30 ~ 2026-10-12 10:00）预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段。如仍需使用，请重新提交预约。', '2026-10-13 13:20:36', 0, '预约已自动作废', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (26, '您预约的「数字示波器」（2026-10-14 08:00 ~ 2026-10-14 10:00）已提交，等待教师审批。', '2026-10-13 13:22:06', 1, '预约申请已提交', 'SYSTEM', 'superadmin');
INSERT INTO `t_notification` VALUES (27, '超级管理员 申请预约「数字示波器」（2026-10-14 08:00 ~ 2026-10-14 10:00），请及时审批。', '2026-10-13 13:22:06', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (28, '您预约的「数字示波器」（2026-10-14 08:00 ~ 2026-10-14 10:00）已批准，已生成借用单，请使用后按时归还。', '2026-10-13 13:22:15', 1, '预约申请已通过', 'SYSTEM', 'superadmin');
INSERT INTO `t_notification` VALUES (29, '您预约的「逻辑分析仪」（2026-10-13 12:00 ~ 2026-10-13 15:00）申请已被拒绝。原因：测试', '2026-10-13 13:22:27', 1, '预约申请被拒绝', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (30, '您预约的「逻辑分析仪」（2026-10-14 11:00 ~ 2026-10-14 19:00）已提交，等待教师审批。', '2026-10-13 13:25:09', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (31, '张明 申请预约「逻辑分析仪」（2026-10-14 11:00 ~ 2026-10-14 19:00），请及时审批。', '2026-10-13 13:25:09', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (32, '您借用的「数字示波器」已于 2026-10-14 到期，请尽快归还。', '2026-10-22 23:45:13', 0, '物资逾期提醒', 'OVERDUE', 'superadmin');
INSERT INTO `t_notification` VALUES (33, '您预约的「逻辑分析仪」（2026-10-14 11:00 ~ 2026-10-14 19:00）预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段。如仍需使用，请重新提交预约。', '2026-10-22 23:45:13', 0, '预约已自动作废', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (34, '您预约的「信号发生器」（2026-10-24 07:00 ~ 2026-10-24 09:00）已提交，等待教师审批。', '2026-10-23 19:18:06', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (35, '张明 申请预约「信号发生器」（2026-10-24 07:00 ~ 2026-10-24 09:00），请及时审批。', '2026-10-23 19:18:06', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (36, '您借用的「防静电焊接台」延期7天已批准，新归还日期：2026-10-20', '2026-10-23 19:21:11', 0, '延期申请已通过', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (37, '您借用的「EDTA二钠盐」申请已被拒绝。原因：', '2026-10-23 19:21:20', 0, '借用申请被拒绝', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (38, '您借用的「防静电焊接台」已于 2026-10-20 到期，请尽快归还。', '2026-10-23 19:38:10', 0, '物资逾期提醒', 'OVERDUE', 'zhangming');
INSERT INTO `t_notification` VALUES (39, '您预约使用的「数字示波器」时段已结束，系统已自动完成结单。', '2026-10-23 19:42:21', 0, '预约已自动结单', 'SYSTEM', 'superadmin');
INSERT INTO `t_notification` VALUES (40, '您预约使用的「直流稳压电源」时段已结束，系统已自动完成结单。', '2026-10-23 19:42:21', 0, '预约已自动结单', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (41, '您借用的「直流稳压电源」已由导师「王老师」通过，正在等待实验室管理员终审。', '2026-10-23 19:44:53', 0, '借用申请已通过导师审批', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (42, '您预约的「直流稳压电源」（2026-10-26 13:00 ~ 2026-10-26 15:00）已提交，等待教师审批。', '2026-10-23 19:44:53', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (43, '张明 申请预约「直流稳压电源」（2026-10-26 13:00 ~ 2026-10-26 15:00），请及时审批。', '2026-10-23 19:44:53', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (44, '您预约的「直流稳压电源」（2026-10-26 13:00 ~ 2026-10-26 15:00）已批准，已生成借用单，请使用后按时归还。', '2026-10-23 19:44:53', 0, '预约申请已通过', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (45, '您预约的「信号发生器」（2026-10-24 07:00 ~ 2026-10-24 09:00）已批准，已生成借用单，请使用后按时归还。', '2026-10-23 19:45:01', 0, '预约申请已通过', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (46, '您借用的「数字示波器」申请被退回，请在「借用记录」中修改后重新提交。原因：请完善用途', '2026-10-23 19:46:15', 1, '借用申请被退回修改', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (47, '您借用的「数字示波器」已由导师「王老师」通过，正在等待实验室管理员终审。', '2026-10-23 19:46:15', 1, '借用申请已通过导师审批', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (48, '您借用的「数字示波器」延期2天已批准，新归还日期：2026-10-27', '2026-10-23 19:46:15', 1, '延期申请已通过', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (49, '您预约的「数字示波器」（2026-10-27 18:00 ~ 2026-10-27 20:00）已提交，等待教师审批。', '2026-10-23 19:46:15', 1, '预约申请已提交', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (50, '李华 申请预约「数字示波器」（2026-10-27 18:00 ~ 2026-10-27 20:00），请及时审批。', '2026-10-23 19:46:15', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (51, '「数字示波器」（2026-10-27 18:00 ~ 2026-10-27 20:00）的预约已取消。', '2026-10-23 19:46:15', 1, '预约已取消', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (52, '您预约的「数字示波器」（2026-10-28 18:00 ~ 2026-10-28 20:00）已提交，等待教师审批。', '2026-10-23 19:46:15', 1, '预约申请已提交', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (53, '李华 申请预约「数字示波器」（2026-10-28 18:00 ~ 2026-10-28 20:00），请及时审批。', '2026-10-23 19:46:15', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (54, '您预约的「数字示波器」（2026-11-04 18:00 ~ 2026-11-04 20:00）已提交，等待教师审批。', '2026-10-23 19:46:15', 1, '预约申请已提交', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (55, '李华 申请预约「数字示波器」（2026-11-04 18:00 ~ 2026-11-04 20:00），请及时审批。', '2026-10-23 19:46:15', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (56, '「数字示波器」（2026-10-28 18:00 ~ 2026-10-28 20:00）的预约已取消。', '2026-10-23 19:46:15', 1, '预约已取消', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (57, '「数字示波器」（2026-11-04 18:00 ~ 2026-11-04 20:00）的预约已取消。', '2026-10-23 19:46:15', 1, '预约已取消', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (58, '您借用的「信号发生器」已于 2026-10-24 到期，请尽快归还。', '2026-10-24 19:44:44', 0, '物资逾期提醒', 'OVERDUE', 'zhangming');
INSERT INTO `t_notification` VALUES (59, '您借用的「直流稳压电源」已于 2026-10-24 到期，请尽快归还。', '2026-10-24 19:44:44', 0, '物资逾期提醒', 'OVERDUE', 'superadmin');
INSERT INTO `t_notification` VALUES (60, '您借用的「数字万用表」已于 2026-10-24 到期，请尽快归还。', '2026-10-24 19:44:44', 0, '物资逾期提醒', 'OVERDUE', 'superadmin');
INSERT INTO `t_notification` VALUES (61, '您借用的「信号发生器」已于 2026-10-24 到期，请尽快归还。', '2026-10-24 19:44:44', 0, '物资逾期提醒', 'OVERDUE', 'superadmin');
INSERT INTO `t_notification` VALUES (62, '您预约使用的「信号发生器」时段已结束，系统已自动完成结单。', '2026-10-24 19:44:44', 0, '预约已自动结单', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (63, '您预约的「热风枪」（2026-10-25 09:00 ~ 2026-10-25 11:00）已提交，等待教师审批。', '2026-10-25 08:32:24', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (64, '张明 申请预约「热风枪」（2026-10-25 09:00 ~ 2026-10-25 11:00），请及时审批。', '2026-10-25 08:32:24', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (65, '您预约的「直流稳压电源」（2026-10-25 11:00 ~ 2026-10-25 13:00）已提交，等待教师审批。', '2026-10-25 08:32:59', 0, '预约申请已提交', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (66, '李华 申请预约「直流稳压电源」（2026-10-25 11:00 ~ 2026-10-25 13:00），请及时审批。', '2026-10-25 08:32:59', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (67, '您预约的「数字万用表」（2026-10-25 13:00 ~ 2026-10-25 15:00）已提交，等待教师审批。', '2026-10-25 09:39:16', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (68, '张明 申请预约「数字万用表」（2026-10-25 13:00 ~ 2026-10-25 15:00），请及时审批。', '2026-10-25 09:39:16', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (69, '您预约的「数字万用表」（2026-10-25 15:00 ~ 2026-10-25 17:00）已提交，等待教师审批。', '2026-10-25 09:39:36', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (70, '张明 申请预约「数字万用表」（2026-10-25 15:00 ~ 2026-10-25 17:00），请及时审批。', '2026-10-25 09:39:36', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (71, '您预约的「数字万用表」（2026-11-01 15:00 ~ 2026-11-01 17:00）已提交，等待教师审批。', '2026-10-25 09:39:36', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (72, '张明 申请预约「数字万用表」（2026-11-01 15:00 ~ 2026-11-01 17:00），请及时审批。', '2026-10-25 09:39:36', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (73, '您预约的「数字万用表」（2026-10-25 15:00 ~ 2026-10-25 17:00）已批准，已生成借用单，请使用后按时归还。', '2026-10-25 09:42:30', 0, '预约申请已通过', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (74, '您预约的「数字万用表」（2026-11-01 15:00 ~ 2026-11-01 17:00）申请已被拒绝。原因：实验6', '2026-10-25 09:42:41', 0, '预约申请被拒绝', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (75, '您借用的「直流稳压电源」已于 2026-10-26 到期，请尽快归还。', '2026-10-26 23:48:03', 0, '物资逾期提醒', 'OVERDUE', 'zhangming');
INSERT INTO `t_notification` VALUES (76, '您借用的「数字万用表」已于 2026-10-25 到期，请尽快归还。', '2026-10-26 23:48:03', 0, '物资逾期提醒', 'OVERDUE', 'zhangming');
INSERT INTO `t_notification` VALUES (77, '您借用的「直流稳压电源」已于 2026-10-25 到期，请尽快归还。', '2026-10-26 23:48:03', 0, '物资逾期提醒', 'OVERDUE', 'teacher');
INSERT INTO `t_notification` VALUES (78, '您借用的「数字示波器」已于 2026-10-25 到期，请尽快归还。', '2026-10-26 23:48:03', 0, '物资逾期提醒', 'OVERDUE', 'teacher');
INSERT INTO `t_notification` VALUES (79, '您借用的「数字示波器」已于 2026-10-25 到期，请尽快归还。', '2026-10-26 23:48:03', 0, '物资逾期提醒', 'OVERDUE', 'superadmin');
INSERT INTO `t_notification` VALUES (80, '您借用的「数字示波器」已于 2026-10-25 到期，请尽快归还。', '2026-10-26 23:48:03', 0, '物资逾期提醒', 'OVERDUE', 'teacher');
INSERT INTO `t_notification` VALUES (81, '您预约的「数字万用表」（2026-10-25 13:00 ~ 2026-10-25 15:00）预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段。如仍需使用，请重新提交预约。', '2026-10-26 23:48:03', 0, '预约已自动作废', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (82, '您预约的「直流稳压电源」（2026-10-25 11:00 ~ 2026-10-25 13:00）预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段。如仍需使用，请重新提交预约。', '2026-10-26 23:48:03', 0, '预约已自动作废', 'SYSTEM', 'lihua');
INSERT INTO `t_notification` VALUES (83, '您预约的「热风枪」（2026-10-25 09:00 ~ 2026-10-25 11:00）预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段。如仍需使用，请重新提交预约。', '2026-10-26 23:48:03', 0, '预约已自动作废', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (84, '您预约使用的「数字万用表」时段已结束，系统已自动完成结单。', '2026-10-26 23:48:03', 0, '预约已自动结单', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (85, '您预约使用的「直流稳压电源」时段已结束，系统已自动完成结单。', '2026-10-26 23:48:03', 0, '预约已自动结单', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (86, '您预约的「数字万用表」（2026-10-27 08:00 ~ 2026-10-27 09:00）已提交，等待教师审批。', '2026-10-26 23:48:51', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (87, '张明 申请预约「数字万用表」（2026-10-27 08:00 ~ 2026-10-27 09:00），请及时审批。', '2026-10-26 23:48:51', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (88, '您预约的「热风枪」（2026-10-28 11:00 ~ 2026-10-28 19:30）已提交，等待教师审批。', '2026-10-26 23:49:02', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (89, '张明 申请预约「热风枪」（2026-10-28 11:00 ~ 2026-10-28 19:30），请及时审批。', '2026-10-26 23:49:02', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (90, '您预约的「热风枪」（2026-10-28 11:00 ~ 2026-10-28 19:30）已批准，已生成借用单，请使用后按时归还。', '2026-10-27 16:26:58', 0, '预约申请已通过', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (91, '您预约的「直流稳压电源」（2026-10-29 08:00 ~ 2026-10-29 09:00）已提交，等待教师审批。', '2026-10-27 16:28:26', 0, '预约申请已提交', 'SYSTEM', 'zhangming');
INSERT INTO `t_notification` VALUES (92, '张明 申请预约「直流稳压电源」（2026-10-29 08:00 ~ 2026-10-29 09:00），请及时审批。', '2026-10-27 16:28:26', 0, '有新的预约待审批', 'SYSTEM', 'teacher');
INSERT INTO `t_notification` VALUES (93, '您预约的「直流稳压电源」（2026-10-29 08:00 ~ 2026-10-29 09:00）已批准，已生成借用单，请使用后按时归还。', '2026-10-27 16:29:10', 0, '预约申请已通过', 'SYSTEM', 'zhangming');

-- ----------------------------
-- Table structure for t_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `t_operation_log`;
CREATE TABLE `t_operation_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `target_id` bigint(20) NULL DEFAULT NULL,
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_operation_log
-- ----------------------------
INSERT INTO `t_operation_log` VALUES (1, 'RESERVE', '2026-09-17 17:27:59', '申请预约「热风枪」2026-09-17 19:00 ~ 2026-09-17 21:00', NULL, 'zhangming', 5, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (2, 'APPROVE', '2026-09-17 20:04:12', '通过「张明」的预约「热风枪」2026-09-17 19:00 ~ 2026-09-17 21:00，已生成借用单 #19', NULL, '超级管理员', 5, 'RESERVATION', '超级管理员');
INSERT INTO `t_operation_log` VALUES (3, 'BORROW_REQUEST', '2026-09-17 20:04:49', '申请借用「EDTA二钠盐」×1', NULL, '张明', 20, 'BORROW', '张明');
INSERT INTO `t_operation_log` VALUES (4, 'BORROW_REQUEST', '2026-10-11 22:12:52', '申请借用「防静电焊接台」×1', NULL, '张明', 21, 'BORROW', '张明');
INSERT INTO `t_operation_log` VALUES (5, 'APPROVE', '2026-10-11 22:13:12', '通过「张明」借用「防静电焊接台」', NULL, '王老师', 21, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (6, 'EXTEND_REQUEST', '2026-10-11 22:14:53', '申请延期「防静电焊接台」7天', NULL, '王老师', 21, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (7, 'RESERVE', '2026-10-11 22:15:19', '申请预约「直流稳压电源」2026-10-12 07:30 ~ 2026-10-12 10:30', NULL, 'teacher', 6, 'RESERVATION', 'teacher');
INSERT INTO `t_operation_log` VALUES (8, 'APPROVE', '2026-10-11 22:16:13', '通过「王老师」的预约「直流稳压电源」2026-10-12 07:30 ~ 2026-10-12 10:30，已生成借用单 #22', NULL, '王老师', 6, 'RESERVATION', '王老师');
INSERT INTO `t_operation_log` VALUES (9, 'RESERVE', '2026-10-11 22:16:50', '申请预约「数字示波器」2026-10-12 07:30 ~ 2026-10-12 10:00', NULL, 'zhangming', 7, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (10, 'RETURN', '2026-10-11 22:19:23', '归还「热风枪」1 件', NULL, '张明', 19, 'BORROW', '张明');
INSERT INTO `t_operation_log` VALUES (11, 'RETURN', '2026-10-11 22:19:25', '归还「热缩管套装」5 件', NULL, '张明', 5, 'BORROW', '张明');
INSERT INTO `t_operation_log` VALUES (12, 'RETURN', '2026-10-11 22:19:26', '归还「移液管」1 件', NULL, '张明', 12, 'BORROW', '张明');
INSERT INTO `t_operation_log` VALUES (13, 'RETURN', '2026-10-11 22:19:28', '归还「数字示波器」1 件', NULL, '张明', 1, 'BORROW', '张明');
INSERT INTO `t_operation_log` VALUES (14, 'RESERVE', '2026-10-11 22:26:01', '申请预约「逻辑分析仪」2026-10-13 12:00 ~ 2026-10-13 15:00', NULL, 'lihua', 8, 'RESERVATION', 'lihua');
INSERT INTO `t_operation_log` VALUES (15, 'STOCK_IN', '2026-10-11 22:28:54', '创建入库单「IN20261011222854972」，供应商：校内仓库，共 2 种物资 ×2 件', NULL, '超级管理员', 1, 'STOCK_IN', '超级管理员');
INSERT INTO `t_operation_log` VALUES (16, 'AUTO_CANCEL', '2026-10-13 13:20:36', '自动作废「张明」的预约「数字示波器」：预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段', NULL, '系统', 7, 'RESERVATION', 'system');
INSERT INTO `t_operation_log` VALUES (17, 'STOCK_IN', '2026-10-13 13:21:46', '创建入库单「IN20261013132145523」，供应商：测试，共 1 种物资 ×1 件', NULL, '超级管理员', 2, 'STOCK_IN', 'superadmin');
INSERT INTO `t_operation_log` VALUES (18, 'STOCK_IN_REVOKE', '2026-10-13 13:21:50', '撤销入库单「IN20261013132145523」（供应商：测试），已回退库存 1 件', NULL, '超级管理员', 2, 'STOCK_IN', 'superadmin');
INSERT INTO `t_operation_log` VALUES (19, 'RESERVE', '2026-10-13 13:22:06', '申请预约「数字示波器」2026-10-14 08:00 ~ 2026-10-14 10:00', NULL, 'superadmin', 9, 'RESERVATION', 'superadmin');
INSERT INTO `t_operation_log` VALUES (20, 'APPROVE', '2026-10-13 13:22:15', '通过「超级管理员」的预约「数字示波器」2026-10-14 08:00 ~ 2026-10-14 10:00，已生成借用单 #23', NULL, '超级管理员', 9, 'RESERVATION', '超级管理员');
INSERT INTO `t_operation_log` VALUES (21, 'REJECT', '2026-10-13 13:22:27', '拒绝「李华」的预约「逻辑分析仪」', NULL, '超级管理员', 8, 'RESERVATION', '超级管理员');
INSERT INTO `t_operation_log` VALUES (22, 'RESERVE', '2026-10-13 13:25:09', '申请预约「逻辑分析仪」2026-10-14 11:00 ~ 2026-10-14 19:00', NULL, 'zhangming', 10, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (23, 'AUTO_CANCEL', '2026-10-22 23:45:13', '自动作废「张明」的预约「逻辑分析仪」：预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段', NULL, '系统', 10, 'RESERVATION', 'system');
INSERT INTO `t_operation_log` VALUES (24, 'RESERVE', '2026-10-23 19:18:06', '申请预约「信号发生器」2026-10-24 07:00 ~ 2026-10-24 09:00', NULL, 'zhangming', 11, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (25, 'EXTEND_APPROVE', '2026-10-23 19:21:11', '批准「张明」延期「防静电焊接台」7天', NULL, '超级管理员', 21, 'BORROW', '超级管理员');
INSERT INTO `t_operation_log` VALUES (26, 'REJECT', '2026-10-23 19:21:20', '拒绝「张明」借用「EDTA二钠盐」', NULL, '超级管理员', 20, 'BORROW', '超级管理员');
INSERT INTO `t_operation_log` VALUES (27, 'RESERVATION_AUTO_RETURN', '2026-10-23 19:42:21', '预约时段结束自动归还「数字示波器」×1', NULL, 'superadmin', 23, 'BORROW', 'superadmin');
INSERT INTO `t_operation_log` VALUES (28, 'RESERVATION_AUTO_RETURN', '2026-10-23 19:42:21', '预约时段结束自动归还「直流稳压电源」×1', NULL, 'teacher', 22, 'BORROW', 'teacher');
INSERT INTO `t_operation_log` VALUES (29, 'BORROW_SELF', '2026-10-23 19:43:35', '自助借用「数字示波器」×1（教师/管理员免审批）', NULL, '王老师', 47, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (30, 'BORROW_SELF', '2026-10-23 19:43:35', '自助领用「U盘 32GB」×1（教师/管理员免审批）', NULL, '王老师', 48, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (31, 'BORROW_SELF', '2026-10-23 19:43:56', '自助借用「数字示波器」×1（教师/管理员免审批）', NULL, '超级管理员', 49, 'BORROW', '超级管理员');
INSERT INTO `t_operation_log` VALUES (32, 'BORROW_SELF', '2026-10-23 19:43:56', '自助领用「U盘 32GB」×1（教师/管理员免审批）', NULL, '超级管理员', 50, 'BORROW', '超级管理员');
INSERT INTO `t_operation_log` VALUES (33, 'BORROW_SELF', '2026-10-23 19:44:41', '自助借用「数字示波器」×1（教师/管理员免审批）', NULL, '王老师', 51, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (34, 'BORROW_SELF', '2026-10-23 19:44:53', '自助借用「直流稳压电源」×1（教师/管理员免审批）', NULL, '王老师', 52, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (35, 'RETURN', '2026-10-23 19:44:53', '归还「直流稳压电源」1 件', NULL, '王老师', 52, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (36, 'BORROW_SELF', '2026-10-23 19:44:53', '自助领用「U盘 32GB」×1（教师/管理员免审批）', NULL, '王老师', 53, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (37, 'BORROW_SELF', '2026-10-23 19:44:53', '自助借用「直流稳压电源」×1（教师/管理员免审批）', NULL, '超级管理员', 54, 'BORROW', '超级管理员');
INSERT INTO `t_operation_log` VALUES (38, 'RETURN', '2026-10-23 19:44:53', '归还「直流稳压电源」1 件', NULL, '超级管理员', 54, 'BORROW', '超级管理员');
INSERT INTO `t_operation_log` VALUES (39, 'BORROW_REQUEST', '2026-10-23 19:44:53', '申请借用「直流稳压电源」×1（先由导师审批）', NULL, '张明', 55, 'BORROW', '张明');
INSERT INTO `t_operation_log` VALUES (40, 'MENTOR_APPROVE', '2026-10-23 19:44:53', '导师审批通过「张明」借用「直流稳压电源」', NULL, '王老师', 55, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (41, 'APPROVE', '2026-10-23 19:44:53', '通过「张明」借用「直流稳压电源」', NULL, '王老师', 55, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (42, 'RETURN', '2026-10-23 19:44:53', '归还「直流稳压电源」1 件', NULL, '张明', 55, 'BORROW', '张明');
INSERT INTO `t_operation_log` VALUES (43, 'RESERVE', '2026-10-23 19:44:53', '申请预约「直流稳压电源」2026-10-26 13:00 ~ 2026-10-26 15:00', NULL, 'zhangming', 12, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (44, 'APPROVE', '2026-10-23 19:44:53', '通过「张明」的预约「直流稳压电源」2026-10-26 13:00 ~ 2026-10-26 15:00，已生成借用单 #56', NULL, '王老师', 12, 'RESERVATION', '王老师');
INSERT INTO `t_operation_log` VALUES (45, 'STOCK_CHECK', '2026-10-23 19:44:53', '库存盘点：核对 1 项，调整 1 项（盘盈 0，盘亏 8）｜U盘 32GB-8', NULL, '王老师', NULL, 'MATERIAL', 'teacher');
INSERT INTO `t_operation_log` VALUES (46, 'APPROVE', '2026-10-23 19:45:01', '通过「张明」的预约「信号发生器」2026-10-24 07:00 ~ 2026-10-24 09:00，已生成借用单 #57', NULL, '王老师', 11, 'RESERVATION', '王老师');
INSERT INTO `t_operation_log` VALUES (47, 'MATERIAL_STATUS', '2026-10-23 19:45:23', '物资「回归物资1792755922978」状态：可用 → 维修中', NULL, '王老师', 205, 'MATERIAL', 'teacher');
INSERT INTO `t_operation_log` VALUES (48, 'MATERIAL_STATUS', '2026-10-23 19:45:23', '物资「回归物资1792755922978」状态：维修中 → 可用', NULL, '王老师', 205, 'MATERIAL', 'teacher');
INSERT INTO `t_operation_log` VALUES (49, 'STOCK_CHECK', '2026-10-23 19:45:23', '库存盘点：核对 1 项，调整 1 项（盘盈 1，盘亏 0）｜回归物资1792755922978+1', NULL, '王老师', NULL, 'MATERIAL', 'teacher');
INSERT INTO `t_operation_log` VALUES (50, 'STOCK_IN', '2026-10-23 19:45:23', '创建入库单「IN20261023194523954」，供应商：测试供应商，共 1 种物资 ×2 件', NULL, '王老师', 3, 'STOCK_IN', 'teacher');
INSERT INTO `t_operation_log` VALUES (51, 'STOCK_IN_EDIT', '2026-10-23 19:45:23', '修改入库单「IN20261023194523954」，供应商：测试供应商B，共 1 种物资 ×3 件', NULL, '王老师', 3, 'STOCK_IN', 'teacher');
INSERT INTO `t_operation_log` VALUES (52, 'STOCK_IN_REVOKE', '2026-10-23 19:45:23', '撤销入库单「IN20261023194523954」（供应商：测试供应商B），已回退库存 3 件', NULL, '王老师', 3, 'STOCK_IN', 'teacher');
INSERT INTO `t_operation_log` VALUES (53, 'USER_STATUS', '2026-10-23 19:45:23', '账号「回归用户已改（smoke1792755922978）」已停用', NULL, '超级管理员', 9, 'USER', 'superadmin');
INSERT INTO `t_operation_log` VALUES (54, 'ATTACHMENT_UPLOAD', '2026-10-23 19:45:49', '上传附件「note.txt」', NULL, '超级管理员', 1, 'MATERIAL', 'superadmin');
INSERT INTO `t_operation_log` VALUES (55, 'ATTACHMENT_DELETE', '2026-10-23 19:45:49', '删除附件「note.txt」', NULL, '超级管理员', 1, 'MATERIAL', 'superadmin');
INSERT INTO `t_operation_log` VALUES (56, 'BORROW_REQUEST', '2026-10-23 19:46:15', '申请借用「数字示波器」×1（先由导师审批）', NULL, '李华', 58, 'BORROW', '李华');
INSERT INTO `t_operation_log` VALUES (57, 'BORROW_CANCEL', '2026-10-23 19:46:15', '撤回借用申请「数字示波器」×1', NULL, '李华', 58, 'BORROW', '李华');
INSERT INTO `t_operation_log` VALUES (58, 'BORROW_REQUEST', '2026-10-23 19:46:15', '申请借用「数字示波器」×1（先由导师审批）', NULL, '李华', 59, 'BORROW', '李华');
INSERT INTO `t_operation_log` VALUES (59, 'RETURN_FOR_EDIT', '2026-10-23 19:46:15', '退回「李华」的借用申请（数字示波器）修改后重新提交', NULL, '王老师', 59, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (60, 'BORROW_RESUBMIT', '2026-10-23 19:46:15', '修改后重新提交借用申请「数字示波器」×1', NULL, '李华', 59, 'BORROW', '李华');
INSERT INTO `t_operation_log` VALUES (61, 'MENTOR_APPROVE', '2026-10-23 19:46:15', '导师审批通过「李华」借用「数字示波器」', NULL, '王老师', 59, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (62, 'APPROVE', '2026-10-23 19:46:15', '通过「李华」借用「数字示波器」', NULL, '王老师', 59, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (63, 'EXTEND_REQUEST', '2026-10-23 19:46:15', '申请延期「数字示波器」2天', NULL, '李华', 59, 'BORROW', '李华');
INSERT INTO `t_operation_log` VALUES (64, 'EXTEND_APPROVE', '2026-10-23 19:46:15', '批准「李华」延期「数字示波器」2天', NULL, '王老师', 59, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (65, 'RETURN', '2026-10-23 19:46:15', '归还「数字示波器」1 件', NULL, '李华', 59, 'BORROW', '李华');
INSERT INTO `t_operation_log` VALUES (66, 'RESERVE', '2026-10-23 19:46:15', '申请预约「数字示波器」2026-10-27 18:00 ~ 2026-10-27 20:00', NULL, 'lihua', 13, 'RESERVATION', 'lihua');
INSERT INTO `t_operation_log` VALUES (67, 'CANCEL', '2026-10-23 19:46:15', '取消预约「数字示波器」', NULL, 'lihua', 13, 'RESERVATION', 'lihua');
INSERT INTO `t_operation_log` VALUES (68, 'RESERVE', '2026-10-23 19:46:15', '申请预约「数字示波器」2026-10-28 18:00 ~ 2026-10-28 20:00', NULL, 'lihua', 14, 'RESERVATION', 'lihua');
INSERT INTO `t_operation_log` VALUES (69, 'RESERVE', '2026-10-23 19:46:15', '申请预约「数字示波器」2026-11-04 18:00 ~ 2026-11-04 20:00', NULL, 'lihua', 15, 'RESERVATION', 'lihua');
INSERT INTO `t_operation_log` VALUES (70, 'CANCEL', '2026-10-23 19:46:15', '取消预约「数字示波器」', NULL, 'lihua', 14, 'RESERVATION', 'lihua');
INSERT INTO `t_operation_log` VALUES (71, 'CANCEL', '2026-10-23 19:46:15', '取消预约「数字示波器」', NULL, 'lihua', 15, 'RESERVATION', 'lihua');
INSERT INTO `t_operation_log` VALUES (72, 'TRAINING_GRANT', '2026-10-23 19:46:15', '登记培训资质：「李华」可操作「数字示波器」，有效期至 2027-10-23', NULL, '王老师', 207, 'MATERIAL', 'teacher');
INSERT INTO `t_operation_log` VALUES (73, 'TRAINING_REVOKE', '2026-10-23 19:46:15', '撤销培训资质：「李华」的「数字示波器」', NULL, '王老师', 207, 'MATERIAL', 'teacher');
INSERT INTO `t_operation_log` VALUES (74, 'BORROW_SELF', '2026-10-23 19:47:55', '自助借用「直流稳压电源」×1（教师/管理员免审批）', NULL, '王老师', 60, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (75, 'BORROW_SELF', '2026-10-23 19:47:55', '自助领用「U盘 32GB」×1（教师/管理员免审批）', NULL, '王老师', 61, 'BORROW', '王老师');
INSERT INTO `t_operation_log` VALUES (76, 'ATTACHMENT_UPLOAD', '2026-10-23 19:47:56', '上传附件「a.txt」', NULL, '超级管理员', 1, 'MATERIAL', 'superadmin');
INSERT INTO `t_operation_log` VALUES (77, 'ATTACHMENT_DELETE', '2026-10-23 19:47:56', '删除附件「a.txt」', NULL, '超级管理员', 1, 'MATERIAL', 'superadmin');
INSERT INTO `t_operation_log` VALUES (78, 'BORROW_SELF', '2026-10-23 19:53:33', '自助借用「直流稳压电源」×1（教师/管理员免审批）', NULL, '超级管理员', 62, 'BORROW', '超级管理员');
INSERT INTO `t_operation_log` VALUES (79, 'BORROW_SELF', '2026-10-23 19:53:33', '自助借用「数字万用表」×1（教师/管理员免审批）', NULL, '超级管理员', 63, 'BORROW', '超级管理员');
INSERT INTO `t_operation_log` VALUES (80, 'BORROW_SELF', '2026-10-23 19:53:33', '自助借用「信号发生器」×1（教师/管理员免审批）', NULL, '超级管理员', 64, 'BORROW', '超级管理员');
INSERT INTO `t_operation_log` VALUES (81, 'RESERVATION_AUTO_RETURN', '2026-10-24 19:44:44', '预约时段结束自动归还「信号发生器」×1', NULL, 'zhangming', 57, 'BORROW', 'zhangming');
INSERT INTO `t_operation_log` VALUES (82, 'BORROW_BATCH_REQUEST', '2026-10-24 19:45:36', '提交批量借用申请 BR202610241945359936，共 2 项物资', NULL, '张明', 65, 'BORROW', '张明');
INSERT INTO `t_operation_log` VALUES (83, 'RESERVE', '2026-10-25 08:32:24', '申请预约「热风枪」2026-10-25 09:00 ~ 2026-10-25 11:00', NULL, 'zhangming', 16, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (84, 'RESERVE', '2026-10-25 08:32:59', '申请预约「直流稳压电源」2026-10-25 11:00 ~ 2026-10-25 13:00', NULL, 'lihua', 17, 'RESERVATION', 'lihua');
INSERT INTO `t_operation_log` VALUES (85, 'BORROW_BATCH_REQUEST', '2026-10-25 09:38:00', '提交批量借用申请 BR202610250937598209，共 2 项物资', NULL, '张明', 67, 'BORROW', '张明');
INSERT INTO `t_operation_log` VALUES (86, 'RESERVE', '2026-10-25 09:39:16', '申请预约「数字万用表」2026-10-25 13:00 ~ 2026-10-25 15:00', NULL, 'zhangming', 18, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (87, 'RESERVE', '2026-10-25 09:39:36', '申请预约「数字万用表」2026-10-25 15:00 ~ 2026-10-25 17:00', NULL, 'zhangming', 19, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (88, 'RESERVE', '2026-10-25 09:39:36', '申请预约「数字万用表」2026-11-01 15:00 ~ 2026-11-01 17:00', NULL, 'zhangming', 20, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (89, 'STOCK_IN', '2026-10-25 09:41:53', '创建入库单「IN20261025094153797」，供应商：校内仓库，共 1 种物资 ×1 件', NULL, '超级管理员', 4, 'STOCK_IN', 'superadmin');
INSERT INTO `t_operation_log` VALUES (90, 'APPROVE', '2026-10-25 09:42:30', '通过「张明」的预约「数字万用表」2026-10-25 15:00 ~ 2026-10-25 17:00，已生成借用单 #69', NULL, '超级管理员', 19, 'RESERVATION', '超级管理员');
INSERT INTO `t_operation_log` VALUES (91, 'REJECT', '2026-10-25 09:42:41', '拒绝「张明」的预约「数字万用表」', NULL, '超级管理员', 20, 'RESERVATION', '超级管理员');
INSERT INTO `t_operation_log` VALUES (92, 'AUTO_CANCEL', '2026-10-26 23:48:03', '自动作废「张明」的预约「数字万用表」：预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段', NULL, '系统', 18, 'RESERVATION', 'system');
INSERT INTO `t_operation_log` VALUES (93, 'AUTO_CANCEL', '2026-10-26 23:48:03', '自动作废「李华」的预约「直流稳压电源」：预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段', NULL, '系统', 17, 'RESERVATION', 'system');
INSERT INTO `t_operation_log` VALUES (94, 'AUTO_CANCEL', '2026-10-26 23:48:03', '自动作废「张明」的预约「热风枪」：预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段', NULL, '系统', 16, 'RESERVATION', 'system');
INSERT INTO `t_operation_log` VALUES (95, 'RESERVATION_AUTO_RETURN', '2026-10-26 23:48:03', '预约时段结束自动归还「数字万用表」×1', NULL, 'zhangming', 69, 'BORROW', 'zhangming');
INSERT INTO `t_operation_log` VALUES (96, 'RESERVATION_AUTO_RETURN', '2026-10-26 23:48:03', '预约时段结束自动归还「直流稳压电源」×1', NULL, 'zhangming', 56, 'BORROW', 'zhangming');
INSERT INTO `t_operation_log` VALUES (97, 'RESERVE', '2026-10-26 23:48:51', '申请预约「数字万用表」2026-10-27 08:00 ~ 2026-10-27 09:00', NULL, 'zhangming', 21, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (98, 'RESERVE', '2026-10-26 23:49:02', '申请预约「热风枪」2026-10-28 11:00 ~ 2026-10-28 19:30', NULL, 'zhangming', 22, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (99, 'APPROVE', '2026-10-27 16:26:58', '通过「张明」的预约「热风枪」2026-10-28 11:00 ~ 2026-10-28 19:30，已生成借用单 #70', NULL, '超级管理员', 22, 'RESERVATION', '超级管理员');
INSERT INTO `t_operation_log` VALUES (100, 'RESERVE', '2026-10-27 16:28:26', '申请预约「直流稳压电源」2026-10-29 08:00 ~ 2026-10-29 09:00', NULL, 'zhangming', 23, 'RESERVATION', 'zhangming');
INSERT INTO `t_operation_log` VALUES (101, 'APPROVE', '2026-10-27 16:29:10', '通过「张明」的预约「直流稳压电源」2026-10-29 08:00 ~ 2026-10-29 09:00，已生成借用单 #71', NULL, '王老师', 23, 'RESERVATION', '王老师');

-- ----------------------------
-- Table structure for t_reservation
-- ----------------------------
DROP TABLE IF EXISTS `t_reservation`;
CREATE TABLE `t_reservation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attendee_count` int(11) NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NOT NULL,
  `material_category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `material_id` bigint(20) NOT NULL,
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `purpose` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `recurring_rule` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `approve_time` datetime(6) NULL DEFAULT NULL,
  `approver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `borrow_record_id` bigint(20) NULL DEFAULT NULL,
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `estimated_fee` double NULL DEFAULT NULL,
  `used_hours` double NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_reservation
-- ----------------------------
INSERT INTO `t_reservation` VALUES (1, 1, '2026-09-15 09:57:39', '2026-09-15 13:00:00', 'DEVICE', 1, '数字示波器', '1（第1次）', '张明', 'WEEKLY', '2026-09-15 10:30:00', 'APPROVED', 'RECURRING', 'zhangming', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_reservation` VALUES (2, 1, '2026-09-15 09:57:39', '2026-09-22 13:00:00', 'DEVICE', 1, '数字示波器', '1（第2次）', '张明', 'WEEKLY', '2026-09-22 10:30:00', 'APPROVED', 'RECURRING', 'zhangming', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_reservation` VALUES (3, 1, '2026-09-15 09:57:39', '2026-09-29 13:00:00', 'DEVICE', 1, '数字示波器', '1（第3次）', '张明', 'WEEKLY', '2026-09-29 10:30:00', 'APPROVED', 'RECURRING', 'zhangming', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_reservation` VALUES (4, 1, '2026-09-15 09:57:39', '2026-10-06 13:00:00', 'DEVICE', 1, '数字示波器', '1（第4次）', '张明', 'WEEKLY', '2026-10-06 10:30:00', 'APPROVED', 'RECURRING', 'zhangming', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `t_reservation` VALUES (5, 1, '2026-09-17 17:27:59', '2026-09-17 21:00:00', 'DEVICE', 7, '热风枪', '', '张明', NULL, '2026-09-17 19:00:00', 'COMPLETED', 'SINGLE', 'zhangming', '2026-09-17 20:04:11.613499', '超级管理员', 19, NULL, NULL, 2);
INSERT INTO `t_reservation` VALUES (6, 1, '2026-10-11 22:15:19', '2026-10-12 10:30:00', 'DEVICE', 2, '直流稳压电源', '实验', '王老师', NULL, '2026-10-12 07:30:00', 'COMPLETED', 'SINGLE', 'teacher', '2026-10-11 22:16:12.567805', '王老师', 22, NULL, NULL, 3);
INSERT INTO `t_reservation` VALUES (7, 1, '2026-10-11 22:16:50', '2026-10-12 10:00:00', 'DEVICE', 1, '数字示波器', '', '张明', NULL, '2026-10-12 07:30:00', 'CANCELLED', 'SINGLE', 'zhangming', NULL, NULL, NULL, '预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段', NULL, NULL);
INSERT INTO `t_reservation` VALUES (8, 1, '2026-10-11 22:26:01', '2026-10-13 15:00:00', 'DEVICE', 6, '逻辑分析仪', '分析', '李华', NULL, '2026-10-13 12:00:00', 'REJECTED', 'SINGLE', 'lihua', '2026-10-13 13:22:27.006492', '超级管理员', NULL, '测试', NULL, NULL);
INSERT INTO `t_reservation` VALUES (9, 1, '2026-10-13 13:22:06', '2026-10-14 10:00:00', 'DEVICE', 1, '数字示波器', '', '超级管理员', NULL, '2026-10-14 08:00:00', 'COMPLETED', 'SINGLE', 'superadmin', '2026-10-13 13:22:15.254162', '超级管理员', 23, NULL, NULL, 2);
INSERT INTO `t_reservation` VALUES (10, 1, '2026-10-13 13:25:09', '2026-10-14 19:00:00', 'DEVICE', 6, '逻辑分析仪', '', '张明', NULL, '2026-10-14 11:00:00', 'CANCELLED', 'SINGLE', 'zhangming', NULL, NULL, NULL, '预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段', NULL, NULL);
INSERT INTO `t_reservation` VALUES (11, 1, '2026-10-23 19:18:06', '2026-10-24 09:00:00', 'DEVICE', 4, '信号发生器', '实验', '张明', NULL, '2026-10-24 07:00:00', 'COMPLETED', 'SINGLE', 'zhangming', '2026-10-23 19:45:01.015602', '王老师', 57, NULL, 0, 2);
INSERT INTO `t_reservation` VALUES (12, 2, '2026-10-23 19:44:53', '2026-10-26 15:00:00', 'DEVICE', 2, '直流稳压电源', '预约回归', '张明', NULL, '2026-10-26 13:00:00', 'COMPLETED', 'SINGLE', 'zhangming', '2026-10-23 19:44:53.196112', '王老师', 56, NULL, 40, 2);
INSERT INTO `t_reservation` VALUES (13, 1, '2026-10-23 19:46:15', '2026-10-27 20:00:00', 'DEVICE', 207, '数字示波器', '取消预约回归', '李华', NULL, '2026-10-27 18:00:00', 'CANCELLED', 'SINGLE', 'lihua', NULL, NULL, NULL, NULL, 0, NULL);
INSERT INTO `t_reservation` VALUES (14, 1, '2026-10-23 19:46:15', '2026-10-28 20:00:00', 'DEVICE', 207, '数字示波器', '周期预约回归（第1次）', '李华', 'WEEKLY', '2026-10-28 18:00:00', 'CANCELLED', 'RECURRING', 'lihua', NULL, NULL, NULL, NULL, 0, NULL);
INSERT INTO `t_reservation` VALUES (15, 1, '2026-10-23 19:46:15', '2026-11-04 20:00:00', 'DEVICE', 207, '数字示波器', '周期预约回归（第2次）', '李华', 'WEEKLY', '2026-11-04 18:00:00', 'CANCELLED', 'RECURRING', 'lihua', NULL, NULL, NULL, NULL, 0, NULL);
INSERT INTO `t_reservation` VALUES (16, 1, '2026-10-25 08:32:24', '2026-10-25 11:00:00', 'DEVICE', 7, '热风枪', '实验', '张明', NULL, '2026-10-25 09:00:00', 'CANCELLED', 'SINGLE', 'zhangming', NULL, NULL, NULL, '预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段', 40, NULL);
INSERT INTO `t_reservation` VALUES (17, 1, '2026-10-25 08:32:59', '2026-10-25 13:00:00', 'DEVICE', 2, '直流稳压电源', '实验2', '李华', NULL, '2026-10-25 11:00:00', 'CANCELLED', 'SINGLE', 'lihua', NULL, NULL, NULL, '预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段', 40, NULL);
INSERT INTO `t_reservation` VALUES (18, 1, '2026-10-25 09:39:16', '2026-10-25 15:00:00', 'DEVICE', 3, '数字万用表', '张明实验', '张明', NULL, '2026-10-25 13:00:00', 'CANCELLED', 'SINGLE', 'zhangming', NULL, NULL, NULL, '预约时段开始后超过 24 小时仍未处理，系统已自动作废并释放该时段', 40, NULL);
INSERT INTO `t_reservation` VALUES (19, 1, '2026-10-25 09:39:36', '2026-10-25 17:00:00', 'DEVICE', 3, '数字万用表', '实验3（第1次）', '张明', 'WEEKLY', '2026-10-25 15:00:00', 'COMPLETED', 'RECURRING', 'zhangming', '2026-10-25 09:42:29.798837', '超级管理员', 69, NULL, 40, 2);
INSERT INTO `t_reservation` VALUES (20, 1, '2026-10-25 09:39:36', '2026-11-01 17:00:00', 'DEVICE', 3, '数字万用表', '实验3（第2次）', '张明', 'WEEKLY', '2026-11-01 15:00:00', 'REJECTED', 'RECURRING', 'zhangming', '2026-10-25 09:42:40.962271', '超级管理员', NULL, '实验6', 40, NULL);
INSERT INTO `t_reservation` VALUES (21, 1, '2026-10-26 23:48:51', '2026-10-27 09:00:00', 'DEVICE', 3, '数字万用表', '实验', '张明', NULL, '2026-10-27 08:00:00', 'PENDING', 'SINGLE', 'zhangming', NULL, NULL, NULL, NULL, 20, NULL);
INSERT INTO `t_reservation` VALUES (22, 1, '2026-10-26 23:49:02', '2026-10-28 19:30:00', 'DEVICE', 7, '热风枪', '实验', '张明', NULL, '2026-10-28 11:00:00', 'APPROVED', 'SINGLE', 'zhangming', '2026-10-27 16:26:57.717352', '超级管理员', 70, NULL, 170, NULL);
INSERT INTO `t_reservation` VALUES (23, 1, '2026-10-27 16:28:26', '2026-10-29 09:00:00', 'DEVICE', 2, '直流稳压电源', '实验', '张明', NULL, '2026-10-29 08:00:00', 'APPROVED', 'SINGLE', 'zhangming', '2026-10-27 16:29:09.637767', '王老师', 71, NULL, 20, NULL);

-- ----------------------------
-- Table structure for t_stock_in
-- ----------------------------
DROP TABLE IF EXISTS `t_stock_in`;
CREATE TABLE `t_stock_in`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `in_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `supplier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `total_amount` double NULL DEFAULT NULL,
  `total_quantity` int(11) NOT NULL,
  `revoke_operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `revoke_time` datetime(6) NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_ej2jj1163394tee1ith9hnyjs`(`in_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_stock_in
-- ----------------------------
INSERT INTO `t_stock_in` VALUES (1, '2026-10-11 22:28:54.099231', 'IN20261011222854972', '超级管理员', '', '校内仓库', 0, 2, NULL, NULL, NULL);
INSERT INTO `t_stock_in` VALUES (2, '2026-10-13 13:21:45.765481', 'IN20261013132145523', '超级管理员', '', '测试', 100, 1, '超级管理员', '2026-10-13 13:21:49.993474', 'REVOKED');
INSERT INTO `t_stock_in` VALUES (3, '2026-10-23 19:45:23.073381', 'IN20261023194523954', '王老师', '修改', '测试供应商B', 18, 3, '王老师', '2026-10-23 19:45:23.131986', 'REVOKED');
INSERT INTO `t_stock_in` VALUES (4, '2026-10-25 09:41:53.359948', 'IN20261025094153797', '超级管理员', '实验4', '校内仓库', 100, 1, NULL, NULL, 'NORMAL');

-- ----------------------------
-- Table structure for t_stock_in_item
-- ----------------------------
DROP TABLE IF EXISTS `t_stock_in_item`;
CREATE TABLE `t_stock_in_item`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `material_id` bigint(20) NULL DEFAULT NULL,
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `quantity` int(11) NOT NULL,
  `specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `stock_in_id` bigint(20) NOT NULL,
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `unit_price` double NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_stock_in_item
-- ----------------------------
INSERT INTO `t_stock_in_item` VALUES (1, 'DEVICE', 7, '热风枪', 1, '快克 850A+', 1, '把', 0);
INSERT INTO `t_stock_in_item` VALUES (2, 'DEVICE', 3, '数字万用表', 1, '福禄克 Fluke 17B+', 1, '个', 0);
INSERT INTO `t_stock_in_item` VALUES (3, 'DEVICE', 2, '直流稳压电源', 1, '普源 DP832A 三通道', 2, '台', 100);
INSERT INTO `t_stock_in_item` VALUES (5, 'CONSUMABLE', 206, '回归入库1792755922978', 3, '50ml', 3, '瓶', 6);
INSERT INTO `t_stock_in_item` VALUES (6, 'DEVICE', 1, '数字示波器', 1, '普源 DS1054Z 50MHz', 4, '台', 100);

-- ----------------------------
-- Table structure for t_stock_log
-- ----------------------------
DROP TABLE IF EXISTS `t_stock_log`;
CREATE TABLE `t_stock_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `after_quantity` int(11) NULL DEFAULT NULL,
  `before_quantity` int(11) NULL DEFAULT NULL,
  `change_quantity` int(11) NOT NULL,
  `change_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `material_id` bigint(20) NOT NULL,
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `ref_id` bigint(20) NULL DEFAULT NULL,
  `ref_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_stock_log
-- ----------------------------
INSERT INTO `t_stock_log` VALUES (1, 3, 4, -1, 'OUT_BORROW', '2026-09-17 20:04:11.612499', 7, '热风枪', '超级管理员', 19, 'BORROW', '预约审批借出：张明');
INSERT INTO `t_stock_log` VALUES (2, 3, 4, -1, 'OUT_BORROW', '2026-10-11 22:13:12.097095', 5, '防静电焊接台', '王老师', 21, 'BORROW', '审批借出：张明');
INSERT INTO `t_stock_log` VALUES (3, 7, 8, -1, 'OUT_BORROW', '2026-10-11 22:16:12.564356', 2, '直流稳压电源', '王老师', 22, 'BORROW', '预约审批借出：王老师');
INSERT INTO `t_stock_log` VALUES (4, 4, 3, 1, 'RETURN', '2026-10-11 22:19:23.407547', 7, '热风枪', '张明', 19, 'BORROW', '归还：张明（1/1 件）');
INSERT INTO `t_stock_log` VALUES (5, 20, 15, 5, 'RETURN', '2026-10-11 22:19:25.098145', 17, '热缩管套装', '张明', 5, 'BORROW', '归还：张明（5/5 件）');
INSERT INTO `t_stock_log` VALUES (6, 11, 10, 1, 'RETURN', '2026-10-11 22:19:26.205045', 48, '移液管', '张明', 12, 'BORROW', '归还：张明（1/1 件）');
INSERT INTO `t_stock_log` VALUES (7, 3, 2, 1, 'RETURN', '2026-10-11 22:19:27.574158', 1, '数字示波器', '张明', 1, 'BORROW', '归还：张明（1/1 件）');
INSERT INTO `t_stock_log` VALUES (8, 5, 4, 1, 'IN', '2026-10-11 22:28:54.103216', 7, '热风枪', '超级管理员', 1, 'STOCK_IN', '采购入库（IN20261011222854972）');
INSERT INTO `t_stock_log` VALUES (9, 12, 11, 1, 'IN', '2026-10-11 22:28:54.115428', 3, '数字万用表', '超级管理员', 1, 'STOCK_IN', '采购入库（IN20261011222854972）');
INSERT INTO `t_stock_log` VALUES (11, 2, 3, -1, 'OUT_BORROW', '2026-10-13 13:22:15.252161', 1, '数字示波器', '超级管理员', 23, 'BORROW', '预约审批借出：超级管理员');
INSERT INTO `t_stock_log` VALUES (15, 3, 2, 1, 'RETURN', '2026-10-23 19:42:21.404740', 1, '数字示波器', '系统', 23, 'BORROW', '预约时段结束自动归还：超级管理员');
INSERT INTO `t_stock_log` VALUES (16, 8, 7, 1, 'RETURN', '2026-10-23 19:42:21.486037', 2, '直流稳压电源', '系统', 22, 'BORROW', '预约时段结束自动归还：王老师');
INSERT INTO `t_stock_log` VALUES (17, 2, 3, -1, 'OUT_BORROW', '2026-10-23 19:43:35.232646', 1, '数字示波器', '王老师', 47, 'BORROW', '自助借出：王老师');
INSERT INTO `t_stock_log` VALUES (18, 13, 14, -1, 'CONSUME', '2026-10-23 19:43:35.312195', 13, 'U盘 32GB', '王老师', 48, 'BORROW', '自助领用：王老师');
INSERT INTO `t_stock_log` VALUES (19, 1, 2, -1, 'OUT_BORROW', '2026-10-23 19:43:55.528837', 1, '数字示波器', '超级管理员', 49, 'BORROW', '自助借出：超级管理员');
INSERT INTO `t_stock_log` VALUES (20, 12, 13, -1, 'CONSUME', '2026-10-23 19:43:55.607280', 13, 'U盘 32GB', '超级管理员', 50, 'BORROW', '自助领用：超级管理员');
INSERT INTO `t_stock_log` VALUES (21, 0, 1, -1, 'OUT_BORROW', '2026-10-23 19:44:41.443535', 1, '数字示波器', '王老师', 51, 'BORROW', '自助借出：王老师');
INSERT INTO `t_stock_log` VALUES (22, 7, 8, -1, 'OUT_BORROW', '2026-10-23 19:44:52.951349', 2, '直流稳压电源', '王老师', 52, 'BORROW', '自助借出：王老师');
INSERT INTO `t_stock_log` VALUES (23, 8, 7, 1, 'RETURN', '2026-10-23 19:44:53.004591', 2, '直流稳压电源', '王老师', 52, 'BORROW', '归还：王老师（1/1 件）');
INSERT INTO `t_stock_log` VALUES (24, 11, 12, -1, 'CONSUME', '2026-10-23 19:44:53.035290', 13, 'U盘 32GB', '王老师', 53, 'BORROW', '自助领用：王老师');
INSERT INTO `t_stock_log` VALUES (25, 7, 8, -1, 'OUT_BORROW', '2026-10-23 19:44:53.050965', 2, '直流稳压电源', '超级管理员', 54, 'BORROW', '自助借出：超级管理员');
INSERT INTO `t_stock_log` VALUES (26, 8, 7, 1, 'RETURN', '2026-10-23 19:44:53.068456', 2, '直流稳压电源', '超级管理员', 54, 'BORROW', '归还：超级管理员（1/1 件）');
INSERT INTO `t_stock_log` VALUES (27, 7, 8, -1, 'OUT_BORROW', '2026-10-23 19:44:53.123207', 2, '直流稳压电源', '王老师', 55, 'BORROW', '审批借出：张明');
INSERT INTO `t_stock_log` VALUES (28, 8, 7, 1, 'RETURN', '2026-10-23 19:44:53.135286', 2, '直流稳压电源', '张明', 55, 'BORROW', '归还：张明（1/1 件）');
INSERT INTO `t_stock_log` VALUES (29, 7, 8, -1, 'OUT_BORROW', '2026-10-23 19:44:53.196112', 2, '直流稳压电源', '王老师', 56, 'BORROW', '预约审批借出：张明');
INSERT INTO `t_stock_log` VALUES (30, 3, 11, -8, 'ADJUST', '2026-10-23 19:44:53.226476', 13, 'U盘 32GB', '王老师', 13, 'STOCK_CHECK', '盘点调整：账面 20 → 实盘 12（盘亏 -8），接口回归盘点');
INSERT INTO `t_stock_log` VALUES (31, 3, 4, -1, 'OUT_BORROW', '2026-10-23 19:45:01.015602', 4, '信号发生器', '王老师', 57, 'BORROW', '预约审批借出：张明');
INSERT INTO `t_stock_log` VALUES (32, 2, 0, 2, 'IN', '2026-10-23 19:45:22.997145', 205, '回归物资1792755922978', '超级管理员', 205, 'MANUAL', '新增物资建档，期初入库');
INSERT INTO `t_stock_log` VALUES (33, 3, 2, 1, 'ADJUST', '2026-10-23 19:45:23.043290', 205, '回归物资1792755922978', '王老师', 205, 'STOCK_CHECK', '盘点调整：账面 2 → 实盘 3（盘盈 +1），回归盘点');
INSERT INTO `t_stock_log` VALUES (36, 2, 0, 2, 'IN', '2026-10-23 19:45:48.135196', 207, '数字示波器', '超级管理员', 207, 'MANUAL', '新增物资建档，期初入库');
INSERT INTO `t_stock_log` VALUES (37, 20, 0, 20, 'IN', '2026-10-23 19:45:48.166963', 208, '杜邦线', '超级管理员', 208, 'MANUAL', '新增物资建档，期初入库');
INSERT INTO `t_stock_log` VALUES (38, 1, 2, -1, 'OUT_BORROW', '2026-10-23 19:46:15.269401', 207, '数字示波器', '王老师', 59, 'BORROW', '审批借出：李华');
INSERT INTO `t_stock_log` VALUES (39, 2, 1, 1, 'RETURN', '2026-10-23 19:46:15.319976', 207, '数字示波器', '李华', 59, 'BORROW', '归还：李华（1/1 件）');
INSERT INTO `t_stock_log` VALUES (40, 6, 7, -1, 'OUT_BORROW', '2026-10-23 19:47:54.876874', 2, '直流稳压电源', '王老师', 60, 'BORROW', '自助借出：王老师');
INSERT INTO `t_stock_log` VALUES (41, 2, 3, -1, 'CONSUME', '2026-10-23 19:47:54.907889', 13, 'U盘 32GB', '王老师', 61, 'BORROW', '自助领用：王老师');
INSERT INTO `t_stock_log` VALUES (42, 2, 0, 2, 'IN', '2026-10-23 19:47:55.420581', 209, '数字示波器', '超级管理员', 209, 'MANUAL', '新增物资建档，期初入库');
INSERT INTO `t_stock_log` VALUES (43, 20, 0, 20, 'IN', '2026-10-23 19:47:55.432208', 210, '杜邦线', '超级管理员', 210, 'MANUAL', '新增物资建档，期初入库');
INSERT INTO `t_stock_log` VALUES (44, 5, 6, -1, 'OUT_BORROW', '2026-10-23 19:53:33.423044', 2, '直流稳压电源', '超级管理员', 62, 'BORROW', '自助借出：超级管理员');
INSERT INTO `t_stock_log` VALUES (45, 11, 12, -1, 'OUT_BORROW', '2026-10-23 19:53:33.441449', 3, '数字万用表', '超级管理员', 63, 'BORROW', '自助借出：超级管理员');
INSERT INTO `t_stock_log` VALUES (46, 2, 3, -1, 'OUT_BORROW', '2026-10-23 19:53:33.449693', 4, '信号发生器', '超级管理员', 64, 'BORROW', '自助借出：超级管理员');
INSERT INTO `t_stock_log` VALUES (47, 3, 2, 1, 'RETURN', '2026-10-24 19:44:43.958889', 4, '信号发生器', '系统', 57, 'BORROW', '预约时段结束自动归还：张明');
INSERT INTO `t_stock_log` VALUES (48, 1, 0, 1, 'IN', '2026-10-25 09:41:53.388271', 1, '数字示波器', '超级管理员', 4, 'STOCK_IN', '采购入库（IN20261025094153797），单价¥100.0');
INSERT INTO `t_stock_log` VALUES (49, 10, 11, -1, 'OUT_BORROW', '2026-10-25 09:42:29.793013', 3, '数字万用表', '超级管理员', 69, 'BORROW', '预约审批借出：张明');
INSERT INTO `t_stock_log` VALUES (50, 11, 10, 1, 'RETURN', '2026-10-26 23:48:03.328393', 3, '数字万用表', '系统', 69, 'BORROW', '预约时段结束自动归还：张明');
INSERT INTO `t_stock_log` VALUES (51, 6, 5, 1, 'RETURN', '2026-10-26 23:48:03.375493', 2, '直流稳压电源', '系统', 56, 'BORROW', '预约时段结束自动归还：张明');
INSERT INTO `t_stock_log` VALUES (52, 4, 5, -1, 'OUT_BORROW', '2026-10-27 16:26:57.712817', 7, '热风枪', '超级管理员', 70, 'BORROW', '预约审批借出：张明');
INSERT INTO `t_stock_log` VALUES (53, 5, 6, -1, 'OUT_BORROW', '2026-10-27 16:29:09.637767', 2, '直流稳压电源', '王老师', 71, 'BORROW', '预约审批借出：张明');

-- ----------------------------
-- Table structure for t_system_config
-- ----------------------------
DROP TABLE IF EXISTS `t_system_config`;
CREATE TABLE `t_system_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_system_config
-- ----------------------------
INSERT INTO `t_system_config` VALUES (1, 'borrow_max_days', '14', '');
INSERT INTO `t_system_config` VALUES (2, 'borrow_max_items', '5', '');
INSERT INTO `t_system_config` VALUES (3, 'reserve_advance_days', '7', '');
INSERT INTO `t_system_config` VALUES (4, 'reserve_min_hours', '1', '');
INSERT INTO `t_system_config` VALUES (5, 'overdue_fine_per_day', '0', '');
INSERT INTO `t_system_config` VALUES (6, 'auto_cancel_hours', '24', '');
INSERT INTO `t_system_config` VALUES (7, 'credit_min_score', '60', '');
INSERT INTO `t_system_config` VALUES (8, 'allow_self_register', '1', '');

-- ----------------------------
-- Table structure for t_training_record
-- ----------------------------
DROP TABLE IF EXISTS `t_training_record`;
CREATE TABLE `t_training_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `material_id` bigint(20) NOT NULL,
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `train_date` date NULL DEFAULT NULL,
  `trainer` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `valid_until` date NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_training_record
-- ----------------------------
INSERT INTO `t_training_record` VALUES (1, '2026-10-22 23:45:13.327187', 8, '频谱分析仪', '张明', '实验室安全与仪器操作培训（演示数据）', '2026-10-12', '王老师', 'zhangming', '2027-10-22');
INSERT INTO `t_training_record` VALUES (2, '2026-10-22 23:45:13.338290', 8, '频谱分析仪', '李华', '实验室安全与仪器操作培训（演示数据）', '2026-10-12', '王老师', 'lihua', '2027-10-22');

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `credit_score` int(11) NULL DEFAULT NULL,
  `department` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `enabled` bit(1) NULL DEFAULT NULL,
  `lab_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mentor_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password_changed` bit(1) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, NULL, '2026-07-30 13:09:39', '$2a$10$HNeQCU43uo7jAq37xacZY.h4N16MKopJ4owYWdKZZzdez16Rzpz9.', '13800000000', '超级管理员', 'SUPER_ADMIN', 'superadmin', 100, '实验中心', b'1', '电子与嵌入式实验室', NULL, b'1');
INSERT INTO `t_user` VALUES (2, NULL, '2026-07-30 13:09:39', '$2a$10$7anYOwRhzjyyQZXkwqDce.errzmEpqnInx14.sBPxANzNPMNkd.eW', '13800000001', '王老师', 'TEACHER', 'teacher', 100, '实验中心', b'1', '电子与嵌入式实验室', NULL, b'1');
INSERT INTO `t_user` VALUES (3, NULL, '2026-07-30 13:09:39', '$2a$10$0Y8RzOpmUEsryoO/RaRbjOIGl59bahxOvc7DY3Hch0dhZEQh76tu2', '13800000002', '张明', 'STUDENT', 'zhangming', 100, '软件学院', b'1', '电子与嵌入式实验室', '王老师', b'1');
INSERT INTO `t_user` VALUES (4, NULL, '2026-07-30 13:09:39', '$2a$10$1HSBAPh0m.qVO/j5joKBWu0VDqN0q4F/K.LhBitbj1ccoc6Oj2L.i', '13800000003', '李华', 'STUDENT', 'lihua', 100, '软件学院', b'1', '电子与嵌入式实验室', '王老师', b'1');
INSERT INTO `t_user` VALUES (5, NULL, '2026-07-30 13:09:39', '$2a$10$YddUdBXXoQBAIBWqDl2v2O3InqZKi2iV3ZjgXfWE2f/C7hghBgL.6', '13800000004', '王芳', 'STUDENT', 'wangfang', 100, '软件学院', b'1', '电子与嵌入式实验室', '王老师', b'1');
INSERT INTO `t_user` VALUES (6, NULL, '2026-07-30 13:09:39', '$2a$10$W6PmEt138Yy9Ny.lHRqkWO8tayI0h1nsFICtCQnq2Jo3W3rzhMwGG', '13800000005', '赵强', 'STUDENT', 'zhaoqiang', 100, '软件学院', b'1', '电子与嵌入式实验室', '王老师', b'1');
INSERT INTO `t_user` VALUES (7, NULL, '2026-07-30 13:09:39', '$2a$10$KSjMUjU5hES3UvGFvA8t9O5c6LdMUhPX62x3Gw.j4sjKXEcq.Inqq', '13800000006', '刘洋', 'STUDENT', 'liuyang', 100, '软件学院', b'1', '电子与嵌入式实验室', '王老师', b'1');
INSERT INTO `t_user` VALUES (8, NULL, '2026-07-30 13:09:39', '$2a$10$yb1wMWqYh6ph6bvMfx6VB.Vj9aRtn.qvEm7hpnf5FhtoH2/vF5UF6', '13800000007', '陈思', 'STUDENT', 'chensi', 100, '软件学院', b'1', '电子与嵌入式实验室', '王老师', b'1');

SET FOREIGN_KEY_CHECKS = 1;

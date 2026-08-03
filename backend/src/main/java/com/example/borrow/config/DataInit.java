package com.example.borrow.config;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.entity.Category;
import com.example.borrow.entity.Material;
import com.example.borrow.entity.SystemConfig;
import com.example.borrow.entity.User;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.CategoryRepository;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.repository.SystemConfigRepository;
import com.example.borrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class DataInit implements CommandLineRunner {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // ====== 初始化用户 ======
        if (userRepository.count() == 0) {
            initUsers();
        }

        // ====== 初始化系统配置 ======
        if (systemConfigRepository.count() == 0) {
            initConfigs();
        }

        // ====== 初始化分类 ======
        if (categoryRepository.count() == 0) {
            initCategories();
        }

        if (materialRepository.count() > 0) {
            // 物资已存在，只检查是否需要补充演示借用记录
            if (borrowRecordRepository.count() == 0) {
                java.util.List<Material> saved = materialRepository.findAll();
                createDemoBorrowRecords(saved);
                System.out.println(">>> 补充初始化演示借用记录完成");
            }
            return;
        }

        // ====== 实验设备 (DEVICE) ======
        Object[][] devices = {
            {"EQ001", "数字示波器", "DEVICE", "普源 DS1054Z 50MHz", "台", 5, "A102电子实验室"},
            {"EQ002", "直流稳压电源", "DEVICE", "普源 DP832A 三通道", "台", 8, "A102电子实验室"},
            {"EQ003", "数字万用表", "DEVICE", "福禄克 Fluke 17B+", "个", 15, "A101基础实验室"},
            {"EQ004", "信号发生器", "DEVICE", "普源 DG1022Z 25MHz", "台", 4, "A102电子实验室"},
            {"EQ005", "防静电焊接台", "DEVICE", "白光 BK881 焊台", "台", 6, "A103焊接实训室"},
            {"EQ006", "逻辑分析仪", "DEVICE", "梦源 LA1010 16通道", "台", 3, "A102电子实验室"},
            {"EQ007", "热风枪", "DEVICE", "快克 850A+", "把", 4, "A103焊接实训室"},
            {"EQ008", "频谱分析仪", "DEVICE", "安捷伦 N9000A 3GHz", "台", 2, "A104通信实验室"},
            {"EQ009", "电子天平", "DEVICE", "梅特勒 ME204 220g/0.1mg", "台", 3, "B201化学实验室"},
            {"EQ010", "离心机", "DEVICE", "湘仪 H1650 16500rpm", "台", 2, "B202生物实验室"},
            {"EQ011", "pH计", "DEVICE", "梅特勒 FE28 0.01精度", "台", 4, "B201化学实验室"},
            {"EQ012", "磁力搅拌器", "DEVICE", "IKA C-MAG 5L", "台", 6, "B201化学实验室"},
        };

        // ====== 耗材 (CONSUMABLE) ======
        Object[][] consumables = {
            {"CS001", "U盘 32GB", "CONSUMABLE", "闪迪 CZ48 32GB USB3.0", "个", 20, "A101耗材柜"},
            {"CS002", "透明胶带", "CONSUMABLE", "3M 18mm宽 透明", "卷", 30, "A101耗材柜"},
            {"CS003", "杜邦线", "CONSUMABLE", "公母头 20cm 40芯", "排", 50, "A103耗材柜"},
            {"CS004", "焊锡丝", "CONSUMABLE", "千住 0.8mm 含松香 1kg", "卷", 15, "A103耗材柜"},
            {"CS005", "热缩管套装", "CONSUMABLE", "2-10mm 各规格 600mm长", "套", 25, "A103耗材柜"},
            {"CS006", "实验记录本", "CONSUMABLE", "A4 80页 方格内页", "本", 50, "A101耗材柜"},
            {"CS007", "标签纸", "CONSUMABLE", "60×40mm 白色不干胶", "张", 200, "A101耗材柜"},
            {"CS008", "尼龙扎带", "CONSUMABLE", "3×100mm 黑色 100根/包", "包", 40, "A103耗材柜"},
            {"CS009", "松香助焊剂", "CONSUMABLE", "固体松香 50g", "盒", 20, "A103耗材柜"},
            {"CS010", "无水乙醇", "CONSUMABLE", "分析纯 500ml", "瓶", 30, "B201化学试剂柜"},
            {"CS011", "脱脂棉", "CONSUMABLE", "医用级 100g/包", "包", 25, "B201耗材柜"},
            {"CS012", "滤纸", "CONSUMABLE", "定性 Φ9cm 100张/盒", "盒", 20, "B201耗材柜"},
            {"CS013", "乳胶手套", "CONSUMABLE", "一次性 无粉 M码 100只", "盒", 15, "B202防护用品柜"},
            {"CS014", "口罩", "CONSUMABLE", "一次性医用 50只/盒", "盒", 20, "B202防护用品柜"},
            {"CS015", "电池 AA", "CONSUMABLE", "南孚 5号 1.5V 4粒装", "组", 30, "A101耗材柜"},
        };

        // ====== 实验用水 (WATER) ======
        Object[][] waters = {
            {"WT001", "去离子水", "WATER", "18.2MΩ·cm 超纯水", "升", 40, "B201纯水间"},
            {"WT002", "蒸馏水", "WATER", "二次蒸馏 实验级", "升", 50, "B201纯水间"},
            {"WT003", "超纯水", "WATER", "Milli-Q 18.2MΩ·cm TOC<5ppb", "升", 20, "B201超纯水机"},
            {"WT004", "注射用水", "WATER", "药典级 WFI", "升", 10, "B203制剂室"},
        };

        // ====== 化学试剂 (REAGENT) ======
        Object[][] reagents = {
            {"RG001", "氢氧化钠", "REAGENT", "分析纯 AR 500g", "瓶", 12, "B201化学试剂柜"},
            {"RG002", "盐酸", "REAGENT", "分析纯 AR 36%-38% 500ml", "瓶", 15, "B201化学试剂柜（酸）"},
            {"RG003", "硫酸", "REAGENT", "分析纯 AR 95%-98% 500ml", "瓶", 10, "B201化学试剂柜（酸）"},
            {"RG004", "丙酮", "REAGENT", "分析纯 AR 500ml", "瓶", 8, "B201化学试剂柜（易燃）"},
            {"RG005", "无水碳酸钠", "REAGENT", "基准试剂 250g", "瓶", 10, "B201化学试剂柜"},
            {"RG006", "氯化钠", "REAGENT", "分析纯 AR 500g", "瓶", 15, "B201化学试剂柜"},
            {"RG007", "硫酸铜", "REAGENT", "分析纯 AR 500g", "瓶", 8, "B201化学试剂柜"},
            {"RG008", "酚酞指示剂", "REAGENT", "0.5% 乙醇溶液 100ml", "瓶", 12, "B201化学试剂柜"},
            {"RG009", "甲基橙指示剂", "REAGENT", "0.1% 水溶液 100ml", "瓶", 10, "B201化学试剂柜"},
            {"RG010", "EDTA二钠盐", "REAGENT", "分析纯 AR 250g", "瓶", 8, "B201化学试剂柜"},
        };

        // ====== 玻璃器皿 (GLASSWARE) ======
        Object[][] glassware = {
            {"GW001", "烧杯", "GLASSWARE", "硼硅玻璃 250ml", "个", 40, "B201玻璃器皿柜"},
            {"GW002", "烧杯", "GLASSWARE", "硼硅玻璃 500ml", "个", 30, "B201玻璃器皿柜"},
            {"GW003", "量筒", "GLASSWARE", "玻璃 100ml", "个", 20, "B201玻璃器皿柜"},
            {"GW004", "容量瓶", "GLASSWARE", "棕色 250ml", "个", 15, "B201玻璃器皿柜"},
            {"GW005", "锥形瓶", "GLASSWARE", "硼硅玻璃 250ml", "个", 25, "B201玻璃器皿柜"},
            {"GW006", "滴定管", "GLASSWARE", "酸式 50ml 0.1精度", "支", 8, "B201玻璃器皿柜"},
            {"GW007", "移液管", "GLASSWARE", "大肚型 10ml A级", "支", 12, "B201玻璃器皿柜"},
            {"GW008", "试管", "GLASSWARE", "玻璃 20×200mm", "支", 100, "B201玻璃器皿柜"},
            {"GW009", "玻璃漏斗", "GLASSWARE", "标准 60mm", "个", 15, "B201玻璃器皿柜"},
            {"GW010", "干燥器", "GLASSWARE", "真空干燥器 240mm", "个", 5, "B201玻璃器皿柜"},
        };

        java.util.List<Material> saved = new java.util.ArrayList<>();
        saved.addAll(saveItems(devices));
        saved.addAll(saveItems(consumables));
        saved.addAll(saveItems(waters));
        saved.addAll(saveItems(reagents));
        saved.addAll(saveItems(glassware));

        System.out.println(">>> 初始化样本数据完成，共 " + saved.size() + " 条物料");

        // ====== 演示借用记录 ======
        createDemoBorrowRecords(saved);
        System.out.println(">>> 初始化演示借用记录完成");
    }

    private void initUsers() {
        Object[][] users = {
            // username, password, realName, role, phone, avatar
            {"superadmin", "123456", "超级管理员", "SUPER_ADMIN", "13800000000", ""},
            {"teacher", "123456", "王老师", "TEACHER", "13800000001", ""},
            {"zhangming", "123456", "张明", "STUDENT", "13800000002", ""},
            {"lihua", "123456", "李华", "STUDENT", "13800000003", ""},
            {"wangfang", "123456", "王芳", "STUDENT", "13800000004", ""},
            {"zhaoqiang", "123456", "赵强", "STUDENT", "13800000005", ""},
            {"liuyang", "123456", "刘洋", "STUDENT", "13800000006", ""},
            {"chensi", "123456", "陈思", "STUDENT", "13800000007", ""},
        };
        for (Object[] u : users) {
            User user = new User();
            user.setUsername((String) u[0]);
            user.setPassword(passwordEncoder.encode((String) u[1]));
            user.setRealName((String) u[2]);
            user.setRole((String) u[3]);
            user.setPhone((String) u[4]);
            userRepository.save(user);
        }
        System.out.println(">>> 初始化用户数据完成，共 " + users.length + " 个用户");
    }

    private java.util.List<Material> saveItems(Object[][] items) {
        java.util.List<Material> list = new java.util.ArrayList<>();
        for (Object[] item : items) {
            Material m = new Material();
            m.setCode((String) item[0]);
            m.setName((String) item[1]);
            m.setCategory((String) item[2]);
            m.setSpecification((String) item[3]);
            m.setUnit((String) item[4]);
            int qty = (Integer) item[5];
            m.setTotalQuantity(qty);
            m.setAvailableQuantity(qty);
            m.setLocation((String) item[6]);
            // 预警阈值：总量的20%，最少2
            m.setWarningThreshold(Math.max(2, qty / 5));
            materialRepository.save(m);
            list.add(m);
        }
        return list;
    }

    private void createDemoBorrowRecords(java.util.List<Material> materials) {
        String[] borrowers = {"张明", "李华", "王芳", "赵强", "刘洋", "陈思"};
        String[] purposes = {
            "毕业设计实验-信号采集系统", "课程实验-模拟电路", "竞赛备赛-电子设计大赛",
            "科研课题-传感器标定", "课程实验-化学分析", "毕业设计-水质检测",
            "实验室日常维护", "教学演示", "课外创新项目"
        };

        // 借用一些物资来制造"借用中"状态
        int[][] borrowPlan = {
            // materialIndex, borrowerIndex, quantity, purposeIndex, daysAgo
            {0, 0, 1, 0, 2},   // 数字示波器 - 张明
            {2, 1, 2, 1, 1},   // 万用表 - 李华
            {12, 2, 3, 5, 3},  // U盘 - 王芳
            {13, 3, 2, 6, 5},  // 透明胶带 - 赵强
            {16, 0, 5, 0, 1},  // 杜邦线 - 张明
            {19, 4, 2, 7, 4},  // 热缩管 - 刘洋
            {27, 2, 3, 5, 2},  // 去离子水 - 王芳
            {28, 1, 5, 4, 3},  // 蒸馏水 - 李华
            {37, 3, 1, 3, 1},  // 氢氧化钠 - 赵强
            {42, 4, 4, 8, 2},  // 烧杯250ml - 刘洋
            {43, 2, 2, 5, 6},  // 烧杯500ml - 王芳
            {47, 0, 1, 0, 1},  // 移液管 - 张明
        };

        for (int[] plan : borrowPlan) {
            Material m = materials.get(plan[0]);
            int qty = plan[2];

            // 更新可借数量
            m.setAvailableQuantity(m.getAvailableQuantity() - qty);
            materialRepository.save(m);

            BorrowRecord record = new BorrowRecord();
            record.setMaterialId(m.getId());
            record.setMaterialName(m.getName());
            record.setMaterialCategory(m.getCategory());
            record.setBorrower(borrowers[plan[1]]);
            record.setBorrowQuantity(qty);
            record.setStatus("BORROWED");
            record.setBorrowTime(LocalDateTime.now().minusDays(plan[4]));
            record.setExpectedReturnTime(LocalDateTime.now().plusDays(7 - plan[4]));
            record.setPurpose(purposes[plan[3]]);
            borrowRecordRepository.save(record);
        }

        // 制造几条已归还记录
        int[][] returnedPlan = {
            {1, 5, 1, 1, 10, 8},   // 直流稳压电源 - 陈思，10天前借，8天前还
            {3, 4, 1, 2, 14, 12},  // 信号发生器 - 刘洋，14天前借，12天前还
            {14, 2, 2, 5, 7, 5},   // 实验记录本 - 王芳，7天前借，5天前还
            {46, 3, 5, 4, 20, 18}, // 滴定管 - 赵强，20天前借，18天前还
            {36, 0, 2, 0, 5, 3},   // 硫酸铜 - 张明，5天前借，3天前还
            {26, 1, 3, 4, 8, 6},   // 无水乙醇 - 李华，8天前借，6天前还
        };

        for (int[] plan : returnedPlan) {
            Material m = materials.get(plan[0]);
            int qty = plan[2];

            BorrowRecord record = new BorrowRecord();
            record.setMaterialId(m.getId());
            record.setMaterialName(m.getName());
            record.setMaterialCategory(m.getCategory());
            record.setBorrower(borrowers[plan[1]]);
            record.setBorrowQuantity(qty);
            record.setStatus("RETURNED");
            record.setBorrowTime(LocalDateTime.now().minusDays(plan[4]));
            record.setActualReturnTime(LocalDateTime.now().minusDays(plan[5]));
            record.setExpectedReturnTime(LocalDateTime.now().plusDays(7 - plan[4]));
            record.setPurpose(purposes[plan[3]]);
            record.setReturnRemark("完好归还");
            borrowRecordRepository.save(record);
        }
    }

    private void initCategories() {
        String[][] categories = {
            // code, name, sortOrder, returnable, description
            {"DEVICE", "实验设备", "1", "true", "示波器、电源、万用表等电子实验设备"},
            {"CONSUMABLE", "耗材", "2", "false", "U盘、胶带、杜邦线、焊锡丝等消耗品"},
            {"WATER", "实验用水", "3", "false", "去离子水、蒸馏水、超纯水"},
            {"REAGENT", "化学试剂", "4", "false", "酸碱盐试剂、指示剂、有机溶剂"},
            {"GLASSWARE", "玻璃器皿", "5", "true", "烧杯、量筒、试管、滴定管等玻璃制品"},
        };
        for (String[] c : categories) {
            Category cat = new Category();
            cat.setCode(c[0]);
            cat.setName(c[1]);
            cat.setSortOrder(Integer.parseInt(c[2]));
            cat.setReturnable(Boolean.parseBoolean(c[3]));
            cat.setDescription(c[4]);
            categoryRepository.save(cat);
        }
        System.out.println(">>> 初始化分类数据完成，共 " + categories.length + " 个分类");
    }

    private void initConfigs() {
        String[][] configs = {
            {"borrow_max_days", "14", "单次借用最长天数"},
            {"borrow_max_items", "5", "单次最多借用数量"},
            {"reserve_advance_days", "7", "预约最多提前天数"},
            {"reserve_min_hours", "2", "预约最短时长（小时）"},
            {"overdue_fine_per_day", "0", "逾期每日违约金（0=不开启）"},
            {"auto_cancel_hours", "24", "预约未取自动取消（小时，0=不开启）"},
        };
        for (String[] c : configs) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(c[0]);
            config.setConfigValue(c[1]);
            config.setDescription(c[2]);
            systemConfigRepository.save(config);
        }
    }
}

package com.example.borrow;

import com.example.borrow.entity.BorrowRecord;
import com.example.borrow.entity.Material;
import com.example.borrow.entity.Notification;
import com.example.borrow.entity.Reservation;
import com.example.borrow.entity.User;
import com.example.borrow.repository.BorrowRecordRepository;
import com.example.borrow.repository.MaterialRepository;
import com.example.borrow.repository.NotificationRepository;
import com.example.borrow.repository.ReservationRepository;
import com.example.borrow.repository.UserRepository;
import com.example.borrow.security.UserContext;
import com.example.borrow.service.BorrowService;
import com.example.borrow.service.NotificationService;
import com.example.borrow.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CoreFlowIntegrationTest {

    @Autowired private BorrowService borrowService;
    @Autowired private ReservationService reservationService;
    @Autowired private NotificationService notificationService;
    @Autowired private BorrowRecordRepository borrowRecordRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;

    @AfterEach
    void clearContext() {
        UserContext.clear();
    }

    @Test
    void teacherBatchBorrowSharesOneRequestAndWritesStableUsername() {
        User teacher = userRepository.findByUsername("teacher").orElseThrow();
        Material device = materialRepository.findAll().stream()
                .filter(m -> "DEVICE".equals(m.getCategory()))
                .filter(m -> !Boolean.TRUE.equals(m.getApprovalRequired()))
                .filter(m -> !Boolean.TRUE.equals(m.getNeedsTraining()))
                .findFirst().orElseThrow();
        Material consumable = materialRepository.findAll().stream()
                .filter(m -> "CONSUMABLE".equals(m.getCategory()))
                .findFirst().orElseThrow();

        List<BorrowService.BatchLine> lines = Arrays.asList(
                line(device.getId(), 1),
                line(consumable.getId(), 2)
        );
        List<BorrowRecord> records = borrowService.requestBatch(
                teacher.getRealName(), teacher.getUsername(), "综合实验", LocalDateTime.now().plusDays(3),
                lines, true);

        assertEquals(2, records.size());
        assertNotNull(records.get(0).getRequestNo());
        assertEquals(records.get(0).getRequestNo(), records.get(1).getRequestNo());
        assertTrue(records.stream().allMatch(r -> teacher.getUsername().equals(r.getBorrowerUsername())));
        assertTrue(records.stream().allMatch(r -> Arrays.asList("BORROWED", "CONSUMED").contains(r.getStatus())));
    }

    @Test
    void staffSingleBorrowAndConsumeUpdateInventoryWithoutApproval() {
        User teacher = userRepository.findByUsername("teacher").orElseThrow();
        Material device = materialRepository.findAll().stream()
                .filter(m -> "DEVICE".equals(m.getCategory()))
                .filter(m -> !Boolean.TRUE.equals(m.getApprovalRequired()))
                .filter(m -> !Boolean.TRUE.equals(m.getNeedsTraining()))
                .findFirst().orElseThrow();
        Material consumable = materialRepository.findAll().stream()
                .filter(m -> "CONSUMABLE".equals(m.getCategory()))
                .filter(m -> !Boolean.TRUE.equals(m.getHazardous()))
                .findFirst().orElseThrow();
        int deviceBefore = device.getAvailableQuantity();
        int consumableBefore = consumable.getAvailableQuantity();
        UserContext.set(teacher.getUsername(), teacher.getRole(), teacher.getRealName());

        BorrowRecord borrowed = borrowService.borrowByStaff(
                device.getId(), teacher.getRealName(), teacher.getUsername(), 1,
                "教师单项借用回归", LocalDateTime.now().plusDays(2), null);
        BorrowRecord consumed = borrowService.borrowByStaff(
                consumable.getId(), teacher.getRealName(), teacher.getUsername(), 1,
                "教师单项领用回归", null, null);

        assertEquals("BORROWED", borrowed.getStatus());
        assertEquals("CONSUMED", consumed.getStatus());
        assertEquals(deviceBefore - 1,
                materialRepository.findById(device.getId()).orElseThrow().getAvailableQuantity());
        assertEquals(consumableBefore - 1,
                materialRepository.findById(consumable.getId()).orElseThrow().getAvailableQuantity());
    }

    @Test
    void highRiskBatchRequiresAnotherApproverAndCannotBeSelfApproved() {
        User teacher = userRepository.findByUsername("teacher").orElseThrow();
        Material highRisk = materialRepository.findAll().stream()
                .filter(m -> Boolean.TRUE.equals(m.getApprovalRequired()))
                .findFirst().orElseThrow();
        // 本用例只验证“需审批 + 禁止自审”，移除培训前置条件，避免与另一条准入规则混在一起
        highRisk.setNeedsTraining(false);
        materialRepository.save(highRisk);

        UserContext.set(teacher.getUsername(), teacher.getRole(), teacher.getRealName());
        List<BorrowRecord> records = borrowService.requestBatch(
                teacher.getRealName(), teacher.getUsername(), "高值仪器实验",
                LocalDateTime.now().plusDays(2), Collections.singletonList(line(highRisk.getId(), 1)), true);

        assertEquals("PENDING", records.get(0).getStatus());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowService.approveBatch(records.get(0).getRequestNo(), teacher.getRealName()));
        assertTrue(ex.getMessage().contains("不能审批本人"));
    }

    @Test
    void notificationCanOnlyBeReadByOwner() {
        User teacher = userRepository.findByUsername("teacher").orElseThrow();
        User student = userRepository.findByUsername("zhangming").orElseThrow();
        Notification n = notificationService.send(student.getUsername(), "测试通知", "仅本人可见", "SYSTEM");

        UserContext.set(teacher.getUsername(), teacher.getRole(), teacher.getRealName());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> notificationService.markRead(n.getId(), teacher.getUsername()));
        assertTrue(ex.getMessage().contains("权限不足"));
        assertFalse(notificationRepository.findById(n.getId()).orElseThrow().getIsRead());
    }

    @Test
    void teacherReservationForOrdinaryMaterialIsRegisteredWithoutApproval() {
        User teacher = userRepository.findByUsername("teacher").orElseThrow();
        Material material = materialRepository.findAll().stream()
                .filter(m -> "DEVICE".equals(m.getCategory()))
                .filter(m -> !Boolean.TRUE.equals(m.getApprovalRequired()))
                .filter(m -> !Boolean.TRUE.equals(m.getNeedsTraining()))
                .findFirst().orElseThrow();
        UserContext.set(teacher.getUsername(), teacher.getRole(), teacher.getRealName());

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        Reservation r = reservationService.createReservation(material.getId(), teacher.getUsername(),
                teacher.getRealName(), start, start.plusHours(2), "教师自主预约", 1, true);

        assertEquals("APPROVED", r.getStatus());
        assertNotNull(r.getBorrowRecordId());
        BorrowRecord generated = borrowRecordRepository.findById(r.getBorrowRecordId()).orElseThrow();
        assertEquals(teacher.getUsername(), generated.getBorrowerUsername());
    }

    @Test
    void autoCloseReservationReturnsInventoryExactlyOnce() {
        User teacher = userRepository.findByUsername("teacher").orElseThrow();
        Material material = materialRepository.findAll().stream()
                .filter(m -> "DEVICE".equals(m.getCategory()))
                .filter(m -> !Boolean.TRUE.equals(m.getApprovalRequired()))
                .filter(m -> !Boolean.TRUE.equals(m.getNeedsTraining()))
                .findFirst().orElseThrow();
        UserContext.set(teacher.getUsername(), teacher.getRole(), teacher.getRealName());
        int before = material.getAvailableQuantity();
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        Reservation r = reservationService.createReservation(material.getId(), teacher.getUsername(),
                teacher.getRealName(), start, start.plusHours(2), "自动结单测试", 1, true);

        assertTrue(borrowService.autoCloseReservationBorrow(r.getBorrowRecordId()));
        assertEquals(before, materialRepository.findById(material.getId()).orElseThrow().getAvailableQuantity());
        assertFalse(borrowService.autoCloseReservationBorrow(r.getBorrowRecordId()));
        assertEquals(before, materialRepository.findById(material.getId()).orElseThrow().getAvailableQuantity());
    }

    private BorrowService.BatchLine line(Long materialId, int quantity) {
        BorrowService.BatchLine line = new BorrowService.BatchLine();
        line.setMaterialId(materialId);
        line.setQuantity(quantity);
        return line;
    }
}

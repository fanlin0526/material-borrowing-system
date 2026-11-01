package com.example.borrow.repository;

import com.example.borrow.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByRefTypeAndRefIdOrderByIdDesc(String refType, Long refId);
}

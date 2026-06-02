package com.PeterTomko.clientportal.repository;

import com.PeterTomko.clientportal.entity.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    List<FileAttachment> findByProjectId(Long projectId);

    List<FileAttachment> findByProjectIdAndProjectUserId(Long projectId, Long userId);

    Optional<FileAttachment> findByIdAndProjectUserId(Long id, Long userId);
}

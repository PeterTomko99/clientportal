package com.PeterTomko.clientportal.repository;

import com.PeterTomko.clientportal.entity.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    List<FileAttachment> findByProjectId(Long projectId);
}

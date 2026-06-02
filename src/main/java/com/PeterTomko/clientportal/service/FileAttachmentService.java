package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.FileAttachment;
import com.PeterTomko.clientportal.exception.ResourceNotFoundException;
import com.PeterTomko.clientportal.repository.FileAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;

    @Transactional(readOnly = true)
    public List<FileAttachment> getFilesByProjectAndUser(Long projectId, Long userId) {
        return fileAttachmentRepository.findByProjectIdAndProjectUserId(projectId, userId);
    }

    @Transactional(readOnly = true)
    public FileAttachment getFileByIdAndUser(Long id, Long userId) {
        return fileAttachmentRepository.findByIdAndProjectUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    @Transactional
    public FileAttachment save(FileAttachment fileAttachment) {
        return fileAttachmentRepository.save(fileAttachment);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        FileAttachment file = getFileByIdAndUser(id, userId);
        fileAttachmentRepository.delete(file);
    }
}

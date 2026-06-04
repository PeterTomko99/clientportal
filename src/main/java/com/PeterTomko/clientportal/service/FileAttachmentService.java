package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.FileAttachment;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.exception.ResourceNotFoundException;
import com.PeterTomko.clientportal.repository.FileAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileAttachmentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "jpg", "jpeg", "png", "gif", "txt", "xlsx", "csv"
    );

    private final FileAttachmentRepository fileAttachmentRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

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
    public FileAttachment upload(MultipartFile file, Project project) throws IOException {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = getExtension(originalName);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("File type not allowed");
        }

        String storedName = UUID.randomUUID() + "." + extension;
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(storedName));

        FileAttachment attachment = FileAttachment.builder()
                .project(project)
                .fileName(originalName)
                .filePath(storedName)
                .build();

        return fileAttachmentRepository.save(attachment);
    }

    @Transactional
    public FileAttachment save(FileAttachment fileAttachment) {
        return fileAttachmentRepository.save(fileAttachment);
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        FileAttachment file = getFileByIdAndUser(id, userId);
        fileAttachmentRepository.delete(file);
    }
}

package com.PeterTomko.clientportal.dto.file;

import com.PeterTomko.clientportal.entity.FileAttachment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileAttachmentResponse {

    private Long id;
    private Long projectId;
    private String fileName;
    private LocalDateTime uploadedAt;

    public static FileAttachmentResponse from(FileAttachment file) {
        return FileAttachmentResponse.builder()
                .id(file.getId())
                .projectId(file.getProject().getId())
                .fileName(file.getFileName())
                .uploadedAt(file.getUploadedAt())
                .build();
    }
}

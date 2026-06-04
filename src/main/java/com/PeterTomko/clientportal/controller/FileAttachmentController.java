package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.dto.file.FileAttachmentResponse;
import com.PeterTomko.clientportal.entity.FileAttachment;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.security.UserPrincipal;
import com.PeterTomko.clientportal.service.FileAttachmentService;
import com.PeterTomko.clientportal.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/files")
@RequiredArgsConstructor
public class FileAttachmentController {

    private final FileAttachmentService fileAttachmentService;
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<FileAttachmentResponse>> list(@PathVariable Long projectId, @AuthenticationPrincipal UserPrincipal principal) {
        List<FileAttachmentResponse> files = fileAttachmentService.getFilesByProjectAndUser(projectId, principal.getId())
                .stream()
                .map(FileAttachmentResponse::from)
                .toList();
        return ResponseEntity.ok(files);
    }

    @PostMapping
    public ResponseEntity<FileAttachmentResponse> upload(@PathVariable Long projectId, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserPrincipal principal) throws IOException {
        Project project = projectService.getProjectByIdAndUser(projectId, principal.getId());
        FileAttachment saved = fileAttachmentService.upload(file, project);
        return ResponseEntity.status(HttpStatus.CREATED).body(FileAttachmentResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        projectService.getProjectByIdAndUser(projectId, principal.getId());
        fileAttachmentService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}

package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.dto.file.FileAttachmentResponse;
import com.PeterTomko.clientportal.entity.FileAttachment;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.security.UserPrincipal;
import com.PeterTomko.clientportal.service.FileAttachmentService;
import com.PeterTomko.clientportal.service.ProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Tag(name = "Files", description = "Upload and manage files for a project")
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

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long projectId, @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) throws IOException {
        projectService.getProjectByIdAndUser(projectId, principal.getId());
        FileAttachment file = fileAttachmentService.getFileByIdAndUser(id, principal.getId());
        Resource resource = fileAttachmentService.loadResource(file);
        String contentType = Files.probeContentType(Paths.get(file.getFilePath()));
        MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;
        String safeFilename = file.getFileName().replace("\"", "").replace("\n", "").replace("\r", "");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFilename + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        projectService.getProjectByIdAndUser(projectId, principal.getId());
        fileAttachmentService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}

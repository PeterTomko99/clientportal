package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.dto.project.ProjectRequest;
import com.PeterTomko.clientportal.dto.project.ProjectResponse;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.security.UserPrincipal;
import com.PeterTomko.clientportal.service.EmailService;
import com.PeterTomko.clientportal.service.ProjectService;
import com.PeterTomko.clientportal.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Projects", description = "Manage your projects")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProjectResponse> projects = projectService.getProjectsByUser(principal.getId(), pageable)
                .map(ProjectResponse::from);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        Project project = projectService.getProjectByIdAndUser(id, principal.getId());
        return ResponseEntity.ok(ProjectResponse.from(project));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        Project project = Project.builder()
                .user(userService.findById(principal.getId()))
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus())
                .deadline(request.getDeadline())
                .build();
        Project saved = projectService.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        Project project = projectService.getProjectByIdAndUser(id, principal.getId());
        Project.Status oldStatus = project.getStatus();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setDeadline(request.getDeadline());
        Project saved = projectService.save(project);
        if (oldStatus != saved.getStatus()) {
            emailService.sendProjectStatusChanged(principal.getUsername(), principal.getName(), saved.getName(), oldStatus, saved.getStatus());
        }
        return ResponseEntity.ok(ProjectResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        projectService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}

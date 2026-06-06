package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.dto.invoice.InvoiceResponse;
import com.PeterTomko.clientportal.dto.project.ProjectResponse;
import com.PeterTomko.clientportal.dto.user.UserResponse;
import com.PeterTomko.clientportal.service.InvoiceService;
import com.PeterTomko.clientportal.service.ProjectService;
import com.PeterTomko.clientportal.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "Admin-only endpoints for managing all users, projects, and invoices")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ProjectService projectService;
    private final InvoiceService invoiceService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserResponse> users = userService.findAll(pageable).map(UserResponse::from);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/projects")
    public ResponseEntity<Page<ProjectResponse>> listProjects(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProjectResponse> projects = projectService.findAll(pageable).map(ProjectResponse::from);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/invoices")
    public ResponseEntity<Page<InvoiceResponse>> listInvoices(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<InvoiceResponse> invoices = invoiceService.findAll(pageable).map(InvoiceResponse::from);
        return ResponseEntity.ok(invoices);
    }
}

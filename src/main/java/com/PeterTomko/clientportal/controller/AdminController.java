package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.dto.invoice.InvoiceResponse;
import com.PeterTomko.clientportal.dto.project.ProjectResponse;
import com.PeterTomko.clientportal.dto.user.UserResponse;
import com.PeterTomko.clientportal.service.InvoiceService;
import com.PeterTomko.clientportal.service.ProjectService;
import com.PeterTomko.clientportal.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin", description = "Admin-only endpoints for managing all users, projects, and invoices")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ProjectService projectService;
    private final InvoiceService invoiceService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userService.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponse>> listProjects() {
        List<ProjectResponse> projects = projectService.findAll()
                .stream()
                .map(ProjectResponse::from)
                .toList();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponse>> listInvoices() {
        List<InvoiceResponse> invoices = invoiceService.findAll()
                .stream()
                .map(InvoiceResponse::from)
                .toList();
        return ResponseEntity.ok(invoices);
    }
}

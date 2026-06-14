package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.dto.dashboard.DashboardStatsResponse;
import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.repository.InvoiceRepository;
import com.PeterTomko.clientportal.repository.ProjectRepository;
import com.PeterTomko.clientportal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Aggregated stats for the current user")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectRepository projectRepository;
    private final InvoiceRepository invoiceRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> stats(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();

        DashboardStatsResponse stats = DashboardStatsResponse.builder()
                .totalProjects(projectRepository.countByUserId(userId))
                .pendingProjects(projectRepository.countByUserIdAndStatus(userId, Project.Status.PENDING))
                .inProgressProjects(projectRepository.countByUserIdAndStatus(userId, Project.Status.IN_PROGRESS))
                .completedProjects(projectRepository.countByUserIdAndStatus(userId, Project.Status.COMPLETED))
                .totalInvoices(invoiceRepository.countByProjectUserId(userId))
                .unpaidInvoices(invoiceRepository.countByProjectUserIdAndStatus(userId, Invoice.Status.UNPAID))
                .overdueInvoices(invoiceRepository.countByProjectUserIdAndStatus(userId, Invoice.Status.OVERDUE))
                .totalRevenue(invoiceRepository.sumPaidAmountByUserId(userId))
                .build();

        return ResponseEntity.ok(stats);
    }
}

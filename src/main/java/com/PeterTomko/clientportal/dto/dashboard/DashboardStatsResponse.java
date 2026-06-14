package com.PeterTomko.clientportal.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsResponse {

    private long totalProjects;
    private long pendingProjects;
    private long inProgressProjects;
    private long completedProjects;
    private long totalInvoices;
    private long unpaidInvoices;
    private long overdueInvoices;
    private BigDecimal totalRevenue;
}

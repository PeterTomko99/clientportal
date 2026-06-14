package com.PeterTomko.clientportal.dto.invoice;

import com.PeterTomko.clientportal.entity.Invoice;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private BigDecimal amount;
    private LocalDate dueDate;
    private Invoice.Status status;
    private LocalDateTime createdAt;

    public static InvoiceResponse from(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .projectId(invoice.getProject().getId())
                .projectName(invoice.getProject().getName())
                .amount(invoice.getAmount())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}

package com.PeterTomko.clientportal.dto.invoice;

import com.PeterTomko.clientportal.entity.Invoice;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    private Invoice.Status status;
}

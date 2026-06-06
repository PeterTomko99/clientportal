package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.entity.Project;
import com.lowagie.text.DocumentException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfServiceTest {

    private final PdfService pdfService = new PdfService();

    @Test
    void generateInvoicePdf_returnsNonEmptyPdf() throws DocumentException {
        Project project = new Project();
        project.setName("Website Redesign");

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setProject(project);
        invoice.setAmount(BigDecimal.valueOf(1500.00));
        invoice.setDueDate(LocalDate.of(2026, 8, 1));
        invoice.setStatus(Invoice.Status.UNPAID);
        invoice.setCreatedAt(LocalDateTime.of(2026, 6, 1, 0, 0));

        byte[] result = pdfService.generateInvoicePdf(invoice, project, "Peter Tomko");

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generateInvoicePdf_startsWith_pdfMagicBytes() throws DocumentException {
        Project project = new Project();
        project.setName("Logo Design");

        Invoice invoice = new Invoice();
        invoice.setId(2L);
        invoice.setProject(project);
        invoice.setAmount(BigDecimal.valueOf(300.00));
        invoice.setDueDate(LocalDate.now().plusDays(14));
        invoice.setStatus(Invoice.Status.PAID);
        invoice.setCreatedAt(LocalDateTime.now());

        byte[] result = pdfService.generateInvoicePdf(invoice, project, "Jane Smith");

        assertTrue(result.length > 4);
        // valid PDF files always start with %PDF
        assertTrue(result[0] == '%' && result[1] == 'P' && result[2] == 'D' && result[3] == 'F');
    }
}

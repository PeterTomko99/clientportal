package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.dto.invoice.InvoiceRequest;
import com.PeterTomko.clientportal.dto.invoice.InvoiceResponse;
import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.security.UserPrincipal;
import com.PeterTomko.clientportal.service.EmailService;
import com.PeterTomko.clientportal.service.InvoiceService;
import com.PeterTomko.clientportal.service.PdfService;
import com.PeterTomko.clientportal.service.ProjectService;
import com.lowagie.text.DocumentException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Invoices", description = "Manage invoices for a project")
@RestController
@RequestMapping("/api/projects/{projectId}/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ProjectService projectService;
    private final EmailService emailService;
    private final PdfService pdfService;

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> list(@PathVariable Long projectId, @AuthenticationPrincipal UserPrincipal principal) {
        List<InvoiceResponse> invoices = invoiceService.getInvoicesByProjectAndUser(projectId, principal.getId())
                .stream()
                .map(InvoiceResponse::from)
                .toList();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> get(@PathVariable Long projectId, @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        projectService.getProjectByIdAndUser(projectId, principal.getId());
        Invoice invoice = invoiceService.getInvoiceByIdAndUser(id, principal.getId());
        return ResponseEntity.ok(InvoiceResponse.from(invoice));
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@PathVariable Long projectId, @Valid @RequestBody InvoiceRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        Project project = projectService.getProjectByIdAndUser(projectId, principal.getId());
        Invoice invoice = Invoice.builder()
                .project(project)
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .build();
        Invoice saved = invoiceService.save(invoice);
        emailService.sendInvoiceCreated(principal.getUsername(), principal.getName(), project.getName(), saved.getAmount(), saved.getDueDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(InvoiceResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> update(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody InvoiceRequest request, @AuthenticationPrincipal UserPrincipal principal) {
        projectService.getProjectByIdAndUser(projectId, principal.getId());
        Invoice invoice = invoiceService.getInvoiceByIdAndUser(id, principal.getId());
        invoice.setAmount(request.getAmount());
        invoice.setDueDate(request.getDueDate());
        invoice.setStatus(request.getStatus());
        Invoice saved = invoiceService.save(invoice);
        return ResponseEntity.ok(InvoiceResponse.from(saved));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long projectId, @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) throws DocumentException {
        Project project = projectService.getProjectByIdAndUser(projectId, principal.getId());
        Invoice invoice = invoiceService.getInvoiceByIdAndUser(id, principal.getId());
        byte[] pdf = pdfService.generateInvoicePdf(invoice, project, principal.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + invoice.getId() + ".pdf\"")
                .body(pdf);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        projectService.getProjectByIdAndUser(projectId, principal.getId());
        invoiceService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}

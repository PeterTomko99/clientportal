package com.PeterTomko.clientportal.scheduler;

import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.repository.InvoiceRepository;
import com.PeterTomko.clientportal.service.EmailService;
import com.PeterTomko.clientportal.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueInvoiceScheduler {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void markOverdueInvoices() {
        List<Invoice> overdue = invoiceRepository.findOverdueWithDetails(Invoice.Status.UNPAID, LocalDate.now());
        for (Invoice invoice : overdue) {
            invoice.setStatus(Invoice.Status.OVERDUE);
            invoiceService.save(invoice);
            emailService.sendOverdueInvoiceReminder(
                    invoice.getProject().getUser().getEmail(),
                    invoice.getProject().getUser().getName(),
                    invoice.getProject().getName(),
                    invoice.getAmount(),
                    invoice.getDueDate()
            );
            log.info("Marked invoice {} as overdue for project {}", invoice.getId(), invoice.getProject().getName());
        }
    }
}

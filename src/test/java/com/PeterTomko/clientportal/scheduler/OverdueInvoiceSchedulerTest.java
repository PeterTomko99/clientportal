package com.PeterTomko.clientportal.scheduler;

import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.entity.User;
import com.PeterTomko.clientportal.repository.InvoiceRepository;
import com.PeterTomko.clientportal.service.EmailService;
import com.PeterTomko.clientportal.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OverdueInvoiceSchedulerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OverdueInvoiceScheduler scheduler;

    @Test
    void markOverdueInvoices_marksInvoiceOverdueAndSendsEmail() {
        User user = User.builder().id(1L).name("John").email("john@test.com").password("hashed").role(User.Role.CLIENT).build();
        Project project = new Project();
        project.setName("Test Project");
        project.setUser(user);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setProject(project);
        invoice.setAmount(BigDecimal.valueOf(500));
        invoice.setDueDate(LocalDate.now().minusDays(1));
        invoice.setStatus(Invoice.Status.UNPAID);

        when(invoiceRepository.findOverdueWithDetails(eq(Invoice.Status.UNPAID), any(LocalDate.class)))
                .thenReturn(List.of(invoice));
        when(invoiceService.save(invoice)).thenReturn(invoice);

        scheduler.markOverdueInvoices();

        assertEquals(Invoice.Status.OVERDUE, invoice.getStatus());
        verify(invoiceService).save(invoice);
        verify(emailService).sendOverdueInvoiceReminder(
                eq("john@test.com"), eq("John"), eq("Test Project"),
                eq(BigDecimal.valueOf(500)), any(LocalDate.class)
        );
    }

    @Test
    void markOverdueInvoices_doesNothingWhenNoOverdueInvoices() {
        when(invoiceRepository.findOverdueWithDetails(any(), any())).thenReturn(List.of());

        scheduler.markOverdueInvoices();

        verifyNoInteractions(invoiceService, emailService);
    }
}

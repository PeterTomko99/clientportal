package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.Project;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "from", "noreply@test.com");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }

    @Test
    void sendInvoiceCreated_sendsEmail() {
        emailService.sendInvoiceCreated("client@test.com", "John", "Project A", BigDecimal.valueOf(500), LocalDate.now());

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendProjectStatusChanged_sendsEmail() {
        emailService.sendProjectStatusChanged("client@test.com", "John", "Project A", Project.Status.PENDING, Project.Status.IN_PROGRESS);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendOverdueInvoiceReminder_sendsEmail() {
        emailService.sendOverdueInvoiceReminder("client@test.com", "John", "Project A", BigDecimal.valueOf(500), LocalDate.now());

        verify(mailSender).send(any(MimeMessage.class));
    }
}

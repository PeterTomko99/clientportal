package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.entity.Project;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendInvoiceCreated(String toEmail, String userName, String projectName,
                                   BigDecimal amount, LocalDate dueDate) {
        String subject = "New Invoice for " + projectName;
        String body = """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #2c3e50;">New Invoice Created</h2>
                <p>Hi %s,</p>
                <p>A new invoice has been created for project <strong>%s</strong>.</p>
                <table style="border-collapse: collapse; margin: 16px 0;">
                  <tr><td style="padding: 6px 12px; background: #f5f5f5;"><strong>Amount</strong></td><td style="padding: 6px 12px;">$%.2f</td></tr>
                  <tr><td style="padding: 6px 12px; background: #f5f5f5;"><strong>Due Date</strong></td><td style="padding: 6px 12px;">%s</td></tr>
                </table>
                </body></html>
                """.formatted(userName, projectName, amount, dueDate);
        send(toEmail, subject, body);
    }

    @Async
    public void sendProjectStatusChanged(String toEmail, String userName, String projectName,
                                         Project.Status oldStatus, Project.Status newStatus) {
        String subject = "Project Update: " + projectName;
        String body = """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #2c3e50;">Project Status Updated</h2>
                <p>Hi %s,</p>
                <p>The status of project <strong>%s</strong> has changed.</p>
                <table style="border-collapse: collapse; margin: 16px 0;">
                  <tr><td style="padding: 6px 12px; background: #f5f5f5;"><strong>Previous</strong></td><td style="padding: 6px 12px;">%s</td></tr>
                  <tr><td style="padding: 6px 12px; background: #f5f5f5;"><strong>New Status</strong></td><td style="padding: 6px 12px;">%s</td></tr>
                </table>
                </body></html>
                """.formatted(userName, projectName, oldStatus, newStatus);
        send(toEmail, subject, body);
    }

    @Async
    public void sendOverdueInvoiceReminder(String toEmail, String userName, String projectName,
                                           BigDecimal amount, LocalDate dueDate) {
        String subject = "Overdue Invoice: " + projectName;
        String body = """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #e74c3c;">Invoice Overdue</h2>
                <p>Hi %s,</p>
                <p>An invoice for project <strong>%s</strong> is now overdue.</p>
                <table style="border-collapse: collapse; margin: 16px 0;">
                  <tr><td style="padding: 6px 12px; background: #f5f5f5;"><strong>Amount</strong></td><td style="padding: 6px 12px;">$%.2f</td></tr>
                  <tr><td style="padding: 6px 12px; background: #f5f5f5;"><strong>Due Date</strong></td><td style="padding: 6px 12px;">%s</td></tr>
                </table>
                <p>Please settle this invoice at your earliest convenience.</p>
                </body></html>
                """.formatted(userName, projectName, amount, dueDate);
        send(toEmail, subject, body);
    }

    @Async
    public void sendPasswordReset(String toEmail, String userName, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "Reset your password";
        String body = """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                <h2 style="color: #2c3e50;">Password Reset Request</h2>
                <p>Hi %s,</p>
                <p>Click the button below to reset your password. This link expires in <strong>1 hour</strong>.</p>
                <p style="margin: 24px 0;">
                  <a href="%s" style="background:#2c3e50;color:#fff;padding:12px 24px;text-decoration:none;border-radius:4px;">
                    Reset Password
                  </a>
                </p>
                <p style="font-size:12px;color:#888;">If you did not request this, ignore this email.</p>
                </body></html>
                """.formatted(userName, resetLink);
        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}

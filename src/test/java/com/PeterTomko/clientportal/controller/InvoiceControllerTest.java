package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.config.ApplicationConfig;
import com.PeterTomko.clientportal.config.CorsConfig;
import com.PeterTomko.clientportal.config.SecurityConfig;
import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.entity.User;
import com.PeterTomko.clientportal.security.JwtAuthenticationFilter;
import com.PeterTomko.clientportal.security.JwtUtil;
import com.PeterTomko.clientportal.security.UserPrincipal;
import com.PeterTomko.clientportal.service.EmailService;
import com.PeterTomko.clientportal.service.InvoiceService;
import com.PeterTomko.clientportal.service.PdfService;
import com.PeterTomko.clientportal.service.ProjectService;
import com.PeterTomko.clientportal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = InvoiceController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, CorsConfig.class, ApplicationConfig.class, JwtUtil.class, JwtAuthenticationFilter.class})
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private PdfService pdfService;

    @MockBean
    private UserService userService;

    private String token;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).name("Peter").email("peter@test.com").password("hashed").role(User.Role.CLIENT).build();
        when(userService.loadUserByUsername("peter@test.com")).thenReturn(new UserPrincipal(testUser));
        token = jwtUtil.generateToken("peter@test.com", 1L, "CLIENT");
    }

    @Test
    void list_returns403WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/projects/1/invoices"))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_returns200WithInvoices() throws Exception {
        Project project = new Project();
        project.setId(1L);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setProject(project);
        invoice.setAmount(BigDecimal.valueOf(100));
        invoice.setDueDate(LocalDate.of(2026, 9, 1));
        invoice.setStatus(Invoice.Status.UNPAID);
        invoice.setCreatedAt(LocalDateTime.now());

        when(invoiceService.getInvoicesByProjectAndUser(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(invoice)));

        mockMvc.perform(get("/api/projects/1/invoices")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(100));
    }

    @Test
    void create_returns201() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setUser(testUser);
        project.setName("Test Project");

        Invoice saved = new Invoice();
        saved.setId(1L);
        saved.setProject(project);
        saved.setAmount(BigDecimal.valueOf(200));
        saved.setDueDate(LocalDate.of(2026, 9, 1));
        saved.setStatus(Invoice.Status.UNPAID);
        saved.setCreatedAt(LocalDateTime.now());

        when(projectService.getProjectByIdAndUser(eq(1L), eq(1L))).thenReturn(project);
        when(invoiceService.save(any(Invoice.class))).thenReturn(saved);
        doNothing().when(emailService).sendInvoiceCreated(any(), any(), any(), any(), any());

        mockMvc.perform(post("/api/projects/1/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":200.00,\"dueDate\":\"2026-09-01\",\"status\":\"UNPAID\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(200));
    }

    @Test
    void create_returns400WhenAmountMissing() throws Exception {
        mockMvc.perform(post("/api/projects/1/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dueDate\":\"2026-09-01\",\"status\":\"UNPAID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returns200WithPaidStatus() throws Exception {
        Project project = new Project();
        project.setId(1L);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setProject(project);
        invoice.setAmount(BigDecimal.valueOf(200));
        invoice.setDueDate(LocalDate.of(2026, 9, 1));
        invoice.setStatus(Invoice.Status.UNPAID);
        invoice.setCreatedAt(LocalDateTime.now());

        Invoice updated = new Invoice();
        updated.setId(1L);
        updated.setProject(project);
        updated.setAmount(BigDecimal.valueOf(200));
        updated.setDueDate(LocalDate.of(2026, 9, 1));
        updated.setStatus(Invoice.Status.PAID);
        updated.setCreatedAt(LocalDateTime.now());

        when(projectService.getProjectByIdAndUser(eq(1L), eq(1L))).thenReturn(project);
        when(invoiceService.getInvoiceByIdAndUser(eq(1L), eq(1L))).thenReturn(invoice);
        when(invoiceService.save(any(Invoice.class))).thenReturn(updated);

        mockMvc.perform(put("/api/projects/1/invoices/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":200.00,\"dueDate\":\"2026-09-01\",\"status\":\"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void delete_returns204() throws Exception {
        when(projectService.getProjectByIdAndUser(eq(1L), eq(1L))).thenReturn(new Project());
        doNothing().when(invoiceService).delete(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/projects/1/invoices/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}

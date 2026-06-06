package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.config.ApplicationConfig;
import com.PeterTomko.clientportal.config.SecurityConfig;
import com.PeterTomko.clientportal.entity.Invoice;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.entity.User;
import com.PeterTomko.clientportal.security.JwtAuthenticationFilter;
import com.PeterTomko.clientportal.security.JwtUtil;
import com.PeterTomko.clientportal.security.UserPrincipal;
import com.PeterTomko.clientportal.service.InvoiceService;
import com.PeterTomko.clientportal.service.ProjectService;
import com.PeterTomko.clientportal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, ApplicationConfig.class, JwtUtil.class, JwtAuthenticationFilter.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private InvoiceService invoiceService;

    private String adminToken;
    private String clientToken;

    @BeforeEach
    void setUp() {
        User admin = User.builder().id(1L).name("Admin").email("admin@test.com").password("hashed").role(User.Role.ADMIN).build();
        User client = User.builder().id(2L).name("Client").email("client@test.com").password("hashed").role(User.Role.CLIENT).build();

        when(userService.loadUserByUsername("admin@test.com")).thenReturn(new UserPrincipal(admin));
        when(userService.loadUserByUsername("client@test.com")).thenReturn(new UserPrincipal(client));

        adminToken = jwtUtil.generateToken("admin@test.com", 1L);
        clientToken = jwtUtil.generateToken("client@test.com", 2L);
    }

    @Test
    void listUsers_returns200ForAdmin() throws Exception {
        User user = User.builder().id(1L).name("Admin").email("admin@test.com").role(User.Role.ADMIN).createdAt(LocalDateTime.now()).build();
        when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@test.com"));
    }

    @Test
    void listUsers_returns403ForClient() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listProjects_returns200ForAdmin() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setStatus(Project.Status.PENDING);
        project.setCreatedAt(LocalDateTime.now());
        when(projectService.findAll()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/admin/projects")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Project"));
    }

    @Test
    void listInvoices_returns200ForAdmin() throws Exception {
        Project project = new Project();
        project.setId(1L);

        Invoice invoice = new Invoice();
        invoice.setId(1L);
        invoice.setProject(project);
        invoice.setAmount(BigDecimal.valueOf(500));
        invoice.setDueDate(LocalDate.now());
        invoice.setStatus(Invoice.Status.UNPAID);
        invoice.setCreatedAt(LocalDateTime.now());

        when(invoiceService.findAll()).thenReturn(List.of(invoice));

        mockMvc.perform(get("/api/admin/invoices")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listProjects_returns403ForClient() throws Exception {
        mockMvc.perform(get("/api/admin/projects")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }
}

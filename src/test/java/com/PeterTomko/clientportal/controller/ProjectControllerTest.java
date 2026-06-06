package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.config.ApplicationConfig;
import com.PeterTomko.clientportal.config.SecurityConfig;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.entity.User;
import com.PeterTomko.clientportal.exception.ResourceNotFoundException;
import com.PeterTomko.clientportal.security.JwtAuthenticationFilter;
import com.PeterTomko.clientportal.security.JwtUtil;
import com.PeterTomko.clientportal.security.UserPrincipal;
import com.PeterTomko.clientportal.service.EmailService;
import com.PeterTomko.clientportal.service.ProjectService;
import com.PeterTomko.clientportal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProjectController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, ApplicationConfig.class, JwtUtil.class, JwtAuthenticationFilter.class})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserService userService;

    @MockBean
    private EmailService emailService;

    private String token;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("peter@test.com").password("hashed").role(User.Role.CLIENT).build();
        when(userService.loadUserByUsername("peter@test.com")).thenReturn(new UserPrincipal(testUser));
        when(userService.findById(1L)).thenReturn(testUser);
        token = jwtUtil.generateToken("peter@test.com", 1L);
    }

    @Test
    void list_returns403WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_returns200WithProjects() throws Exception {
        Project project = buildProject(1L, "Website Redesign");
        when(projectService.getProjectsByUser(1L)).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Website Redesign"));
    }

    @Test
    void create_returns201WithCreatedProject() throws Exception {
        Project saved = buildProject(1L, "New Project");
        when(projectService.save(any(Project.class))).thenReturn(saved);

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Project\",\"status\":\"PENDING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Project"));
    }

    @Test
    void create_returns400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"status\":\"PENDING\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_returns404WhenProjectNotFound() throws Exception {
        when(projectService.getProjectByIdAndUser(eq(99L), eq(1L)))
                .thenThrow(new ResourceNotFoundException("Project not found"));

        mockMvc.perform(get("/api/projects/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project not found"));
    }

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(projectService).delete(1L, 1L);

        mockMvc.perform(delete("/api/projects/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private Project buildProject(Long id, String name) {
        Project p = new Project();
        p.setId(id);
        p.setName(name);
        p.setUser(testUser);
        p.setStatus(Project.Status.PENDING);
        p.setDeadline(LocalDate.of(2026, 9, 1));
        return p;
    }
}

package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.config.ApplicationConfig;
import com.PeterTomko.clientportal.config.CorsConfig;
import com.PeterTomko.clientportal.config.SecurityConfig;
import com.PeterTomko.clientportal.entity.FileAttachment;
import com.PeterTomko.clientportal.entity.Project;
import com.PeterTomko.clientportal.entity.User;
import com.PeterTomko.clientportal.security.JwtAuthenticationFilter;
import com.PeterTomko.clientportal.security.JwtUtil;
import com.PeterTomko.clientportal.security.UserPrincipal;
import com.PeterTomko.clientportal.service.FileAttachmentService;
import com.PeterTomko.clientportal.service.ProjectService;
import com.PeterTomko.clientportal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = FileAttachmentController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, CorsConfig.class, ApplicationConfig.class, JwtUtil.class, JwtAuthenticationFilter.class})
class FileAttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private FileAttachmentService fileAttachmentService;

    @MockBean
    private ProjectService projectService;

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
        mockMvc.perform(get("/api/projects/1/files"))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_returns200WithFiles() throws Exception {
        Project project = new Project();
        project.setId(1L);

        FileAttachment file = new FileAttachment();
        file.setId(1L);
        file.setProject(project);
        file.setFileName("document.pdf");
        file.setFilePath("uuid-document.pdf");
        file.setUploadedAt(LocalDateTime.now());

        when(fileAttachmentService.getFilesByProjectAndUser(eq(1L), eq(1L)))
                .thenReturn(List.of(file));

        mockMvc.perform(get("/api/projects/1/files")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fileName").value("document.pdf"));
    }

    @Test
    void upload_returns201() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setUser(testUser);

        FileAttachment saved = new FileAttachment();
        saved.setId(1L);
        saved.setProject(project);
        saved.setFileName("test.pdf");
        saved.setFilePath("uuid-test.pdf");
        saved.setUploadedAt(LocalDateTime.now());

        when(projectService.getProjectByIdAndUser(eq(1L), eq(1L))).thenReturn(project);
        when(fileAttachmentService.upload(any(), any())).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy content".getBytes());

        mockMvc.perform(multipart("/api/projects/1/files")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("test.pdf"));
    }

    @Test
    void delete_returns204() throws Exception {
        when(projectService.getProjectByIdAndUser(eq(1L), eq(1L))).thenReturn(new Project());
        doNothing().when(fileAttachmentService).delete(eq(1L), eq(1L));

        mockMvc.perform(delete("/api/projects/1/files/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}

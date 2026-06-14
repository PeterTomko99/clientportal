package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.config.ApplicationConfig;
import com.PeterTomko.clientportal.config.CorsConfig;
import com.PeterTomko.clientportal.config.SecurityConfig;
import com.PeterTomko.clientportal.entity.User;
import com.PeterTomko.clientportal.security.JwtAuthenticationFilter;
import com.PeterTomko.clientportal.security.JwtUtil;
import com.PeterTomko.clientportal.security.UserPrincipal;
import com.PeterTomko.clientportal.service.EmailService;
import com.PeterTomko.clientportal.service.PasswordResetService;
import com.PeterTomko.clientportal.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import({SecurityConfig.class, CorsConfig.class, ApplicationConfig.class, JwtUtil.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private EmailService emailService;

    @Test
    void register_returns201AndToken() throws Exception {
        User saved = User.builder().id(1L).email("peter@test.com").name("Peter").role(User.Role.CLIENT).build();
        when(userService.existsByEmail("peter@test.com")).thenReturn(false);
        when(userService.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Peter\",\"email\":\"peter@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void register_returns409WhenEmailAlreadyTaken() throws Exception {
        when(userService.existsByEmail("peter@test.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Peter\",\"email\":\"peter@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void register_returns400WhenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Peter\",\"email\":\"peter@test.com\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").isNotEmpty());
    }

    @Test
    void register_returns400WhenEmailInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Peter\",\"email\":\"not-an-email\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").isNotEmpty());
    }

    @Test
    void login_returns200AndToken() throws Exception {
        User user = User.builder().id(1L).email("peter@test.com").password("hashed").role(User.Role.CLIENT).build();
        UserPrincipal principal = new UserPrincipal(user);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"peter@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_returns401WithWrongPassword() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"peter@test.com\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}

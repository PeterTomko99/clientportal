package com.PeterTomko.clientportal.controller;

import com.PeterTomko.clientportal.dto.auth.AuthResponse;
import com.PeterTomko.clientportal.dto.auth.ForgotPasswordRequest;
import com.PeterTomko.clientportal.dto.auth.LoginRequest;
import com.PeterTomko.clientportal.dto.auth.RegisterRequest;
import com.PeterTomko.clientportal.dto.auth.ResetPasswordRequest;
import com.PeterTomko.clientportal.entity.User;
import com.PeterTomko.clientportal.repository.UserRepository;
import com.PeterTomko.clientportal.security.JwtUtil;
import com.PeterTomko.clientportal.security.UserPrincipal;
import com.PeterTomko.clientportal.service.EmailService;
import com.PeterTomko.clientportal.service.PasswordResetService;
import com.PeterTomko.clientportal.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Tag(name = "Auth", description = "Register and login")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.CLIENT)
                .build();

        User saved = userService.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getId(), saved.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Optional<String> token = passwordResetService.createToken(request.getEmail());
        token.ifPresent(t -> {
            userRepository.findByEmail(request.getEmail()).ifPresent(user ->
                emailService.sendPasswordReset(user.getEmail(), user.getName(), t)
            );
        });
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = jwtUtil.generateToken(principal.getUsername(), principal.getId(), principal.getRole());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}

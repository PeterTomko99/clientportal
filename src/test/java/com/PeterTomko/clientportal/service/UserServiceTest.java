package com.PeterTomko.clientportal.service;

import com.PeterTomko.clientportal.entity.User;
import com.PeterTomko.clientportal.exception.ResourceNotFoundException;
import com.PeterTomko.clientportal.repository.UserRepository;
import com.PeterTomko.clientportal.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_returnsUserPrincipal() {
        User user = User.builder().id(1L).email("peter@test.com").password("hashed").role(User.Role.CLIENT).build();
        when(userRepository.findByEmail("peter@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("peter@test.com");

        assertInstanceOf(UserPrincipal.class, result);
        assertEquals("peter@test.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_throwsWhenNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("unknown@test.com"));
    }

    @Test
    void findById_returnsUser() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertEquals(user, result);
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(1L));
    }

    @Test
    void existsByEmail_returnsTrue() {
        when(userRepository.existsByEmail("peter@test.com")).thenReturn(true);

        assertTrue(userService.existsByEmail("peter@test.com"));
    }

    @Test
    void existsByEmail_returnsFalse() {
        when(userRepository.existsByEmail("nobody@test.com")).thenReturn(false);

        assertFalse(userService.existsByEmail("nobody@test.com"));
    }
}

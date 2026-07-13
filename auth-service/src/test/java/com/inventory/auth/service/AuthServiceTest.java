package com.inventory.auth.service;

import com.inventory.auth.dto.LoginRequest;
import com.inventory.auth.dto.LoginResponse;
import com.inventory.auth.entity.User;
import com.inventory.auth.enums.Role;
import com.inventory.auth.enums.UserStatus;
import com.inventory.auth.repository.UserRepository;
import com.inventory.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@test.com");
        req.setPassword("password");

        User u = new User();
        u.setEmail("admin@test.com");
        u.setPassword("hashed");
        u.setRole(Role.SYSTEM_ADMIN);
        u.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateToken(u)).thenReturn("token123");

        LoginResponse response = authService.login(req);

        assertEquals("token123", response.getToken());
    }

    @Test
    void testLoginFailure() {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@test.com");
        req.setPassword("wrong");

        User u = new User();
        u.setEmail("admin@test.com");
        u.setPassword("hashed");
        u.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(req));
    }
}

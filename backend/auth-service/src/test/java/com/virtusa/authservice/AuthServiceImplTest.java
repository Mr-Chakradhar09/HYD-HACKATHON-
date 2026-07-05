package com.virtusa.authservice;

import com.virtusa.authservice.dto.request.CreateCredentialRequestDto;
import com.virtusa.authservice.dto.request.LoginRequestDto;
import com.virtusa.authservice.dto.response.LoginResponseDto;
import com.virtusa.authservice.entity.Credential;
import com.virtusa.authservice.exception.InvalidCredentialsException;
import com.virtusa.authservice.repository.CredentialRepository;
import com.virtusa.authservice.security.JwtService;
import com.virtusa.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCredentials_Success() {
        CreateCredentialRequestDto request = CreateCredentialRequestDto.builder()
                .employeeId("EMP100")
                .email("test@virtusa.com")
                .password("password")
                .role("EMPLOYEE")
                .location("US")
                .build();

        when(credentialRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(credentialRepository.existsByEmployeeId(request.getEmployeeId())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        authService.createCredentials(request);

        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    @Test
    void testCreateCredentials_DuplicateEmail() {
        CreateCredentialRequestDto request = CreateCredentialRequestDto.builder()
                .employeeId("EMP100")
                .email("test@virtusa.com")
                .password("password")
                .role("EMPLOYEE")
                .location("US")
                .build();

        when(credentialRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.createCredentials(request));
        verify(credentialRepository, never()).save(any(Credential.class));
    }

    @Test
    void testLogin_Success() {
        LoginRequestDto request = new LoginRequestDto("test@virtusa.com", "password");
        Credential credential = Credential.builder()
                .email("test@virtusa.com")
                .password("encodedPassword")
                .employeeId("EMP100")
                .role("EMPLOYEE")
                .location("US")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        when(credentialRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches(request.getPassword(), credential.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any(), any())).thenReturn("mockToken");

        LoginResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals("EMP100", response.getEmployeeId());
    }

    @Test
    void testLogin_InvalidPassword() {
        LoginRequestDto request = new LoginRequestDto("test@virtusa.com", "wrongPassword");
        Credential credential = Credential.builder()
                .email("test@virtusa.com")
                .password("encodedPassword")
                .enabled(true)
                .accountNonLocked(true)
                .build();

        when(credentialRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches(request.getPassword(), credential.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}

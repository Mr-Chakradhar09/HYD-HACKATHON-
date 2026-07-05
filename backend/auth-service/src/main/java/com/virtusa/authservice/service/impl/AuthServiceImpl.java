package com.virtusa.authservice.service.impl;

import com.virtusa.authservice.dto.request.ChangePasswordRequestDto;
import com.virtusa.authservice.dto.request.CreateCredentialRequestDto;
import com.virtusa.authservice.dto.request.LoginRequestDto;
import com.virtusa.authservice.dto.request.TokenValidationRequestDto;
import com.virtusa.authservice.dto.response.CurrentUserResponseDto;
import com.virtusa.authservice.dto.response.LoginResponseDto;
import com.virtusa.authservice.dto.response.TokenValidationResponseDto;
import com.virtusa.authservice.entity.Credential;
import com.virtusa.authservice.exception.InvalidCredentialsException;
import com.virtusa.authservice.exception.UserNotFoundException;
import com.virtusa.authservice.repository.CredentialRepository;
import com.virtusa.authservice.security.JwtService;
import com.virtusa.authservice.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(CredentialRepository credentialRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public void createCredentials(CreateCredentialRequestDto requestDto) {
        log.info("Creating credentials for employeeId: {} and email: {}", requestDto.getEmployeeId(), requestDto.getEmail());
        
        if (credentialRepository.existsByEmail(requestDto.getEmail())) {
            log.error("Email {} already exists", requestDto.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        if (credentialRepository.existsByEmployeeId(requestDto.getEmployeeId())) {
            log.error("Employee ID {} already exists", requestDto.getEmployeeId());
            throw new IllegalArgumentException("Employee ID already exists");
        }

        Credential credential = Credential.builder()
                .employeeId(requestDto.getEmployeeId())
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(requestDto.getRole().toUpperCase())
                .location(requestDto.getLocation())
                .enabled(true)
                .accountNonLocked(true)
                .build();

        credentialRepository.save(credential);
        log.info("Credentials successfully created for employeeId: {}", requestDto.getEmployeeId());
    }

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto) {
        log.info("Attempting login for email: {}", requestDto.getEmail());

        Credential credential = credentialRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", requestDto.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(requestDto.getPassword(), credential.getPassword())) {
            log.error("Password mismatch for email: {}", requestDto.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!credential.isEnabled()) {
            throw new InvalidCredentialsException("Account is disabled");
        }

        if (!credential.isAccountNonLocked()) {
            throw new InvalidCredentialsException("Account is locked");
        }

        credential.setLastLoginAt(LocalDateTime.now());
        credentialRepository.save(credential);

        String token = jwtService.generateToken(
                credential.getEmail(),
                credential.getEmployeeId(),
                credential.getRole(),
                credential.getLocation()
        );

        log.info("Successfully generated JWT token for email: {}", requestDto.getEmail());

        return LoginResponseDto.builder()
                .token(token)
                .employeeId(credential.getEmployeeId())
                .email(credential.getEmail())
                .role(credential.getRole())
                .location(credential.getLocation())
                .build();
    }

    @Override
    public TokenValidationResponseDto validateToken(TokenValidationRequestDto requestDto) {
        log.info("Validating token");
        String token = requestDto.getToken();
        
        try {
            String email = jwtService.extractUsername(token);
            Credential credential = credentialRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found from token claims"));

            boolean isValid = jwtService.validateToken(token, credential.getEmail());

            if (isValid) {
                return TokenValidationResponseDto.builder()
                        .valid(true)
                        .employeeId(credential.getEmployeeId())
                        .email(credential.getEmail())
                        .role(credential.getRole())
                        .location(credential.getLocation())
                        .build();
            }
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
        }

        return TokenValidationResponseDto.builder().valid(false).build();
    }

    @Override
    public CurrentUserResponseDto getCurrentUser(String email) {
        log.info("Fetching current user for email: {}", email);
        Credential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return CurrentUserResponseDto.builder()
                .employeeId(credential.getEmployeeId())
                .email(credential.getEmail())
                .role(credential.getRole())
                .location(credential.getLocation())
                .build();
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequestDto requestDto) {
        log.info("Changing password for email: {}", email);
        Credential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(requestDto.getOldPassword(), credential.getPassword())) {
            log.error("Old password does not match for email: {}", email);
            throw new InvalidCredentialsException("Old password does not match");
        }

        credential.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        credentialRepository.save(credential);
        log.info("Password successfully changed for email: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String employeeId, String newPassword) {
        log.info("Resetting password for employeeId: {}", employeeId);
        Credential credential = credentialRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UserNotFoundException("User not found with employee ID: " + employeeId));

        credential.setPassword(passwordEncoder.encode(newPassword));
        credentialRepository.save(credential);
        log.info("Password successfully reset for employeeId: {}", employeeId);
    }
}

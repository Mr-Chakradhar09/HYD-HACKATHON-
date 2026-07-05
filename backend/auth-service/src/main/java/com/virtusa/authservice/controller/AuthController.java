package com.virtusa.authservice.controller;

import com.virtusa.authservice.common.ApiResponse;
import com.virtusa.authservice.dto.request.ChangePasswordRequestDto;
import com.virtusa.authservice.dto.request.CreateCredentialRequestDto;
import com.virtusa.authservice.dto.request.LoginRequestDto;
import com.virtusa.authservice.dto.request.TokenValidationRequestDto;
import com.virtusa.authservice.dto.response.CurrentUserResponseDto;
import com.virtusa.authservice.dto.response.LoginResponseDto;
import com.virtusa.authservice.dto.response.TokenValidationResponseDto;
import com.virtusa.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Authentication Controller", description = "Endpoints for login, token validation, password management, and credential creation")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.virtusa.authservice.repository.CredentialRepository credentialRepository;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/login")
    @Operation(summary = "Authenticate user and generate JWT token")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Received login request for email: {}", loginRequestDto.getEmail());
        LoginResponseDto response = authService.login(loginRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/api/auth/validate")
    @Operation(summary = "Validate JWT token and retrieve user details")
    public ResponseEntity<ApiResponse<TokenValidationResponseDto>> validateToken(
            @Valid @RequestBody TokenValidationRequestDto tokenValidationRequestDto) {
        log.info("Received token validation request");
        TokenValidationResponseDto response = authService.validateToken(tokenValidationRequestDto);
        if (response.isValid()) {
            return ResponseEntity.ok(ApiResponse.success("Token is valid", response));
        } else {
            return ResponseEntity.ok(ApiResponse.success("Token is invalid", response));
        }
    }

    @GetMapping("/api/auth/me")
    @Operation(summary = "Get current authenticated user profile info")
    public ResponseEntity<ApiResponse<CurrentUserResponseDto>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received request for current user: {}", userDetails.getUsername());
        CurrentUserResponseDto response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved successfully", response));
    }

    @PostMapping("/api/auth/change-password")
    @Operation(summary = "Change password for the current authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {
        log.info("Received change password request for user: {}", userDetails.getUsername());
        authService.changePassword(userDetails.getUsername(), changePasswordRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @PostMapping("/internal/auth/create-credentials")
    @Operation(summary = "Internal endpoint to create new employee credentials")
    public ResponseEntity<ApiResponse<Void>> createCredentials(
            @Valid @RequestBody CreateCredentialRequestDto createCredentialRequestDto) {
        log.info("Received internal request to create credentials for employeeId: {}", createCredentialRequestDto.getEmployeeId());
        authService.createCredentials(createCredentialRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Credentials created successfully"));
    }

    @GetMapping("/api/auth/debug/credentials")
    @Operation(summary = "Debug endpoint to list all seeded credentials")
    public ResponseEntity<?> debugCredentials() {
        return ResponseEntity.ok(credentialRepository.findAll());
    }
}

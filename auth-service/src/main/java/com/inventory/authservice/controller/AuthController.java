package com.inventory.authservice.controller;

import com.inventory.authservice.dto.request.ChangePasswordRequest;
import com.inventory.authservice.dto.request.LoginRequest;
import com.inventory.authservice.dto.request.RefreshTokenRequest;
import com.inventory.authservice.dto.request.RegisterRequest;
import com.inventory.authservice.dto.response.ApiResponse;
import com.inventory.authservice.dto.response.ChangePasswordResponse;
import com.inventory.authservice.dto.response.LoginResponse;
import com.inventory.authservice.dto.response.RefreshTokenResponse;
import com.inventory.authservice.dto.response.UserResponse;
import com.inventory.authservice.service.AuthenticationService;
import com.inventory.authservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    public AuthController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = userService.register(request);
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("User registered successfully");
        response.setData(userResponse);
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authenticationService.login(request);
        ApiResponse<LoginResponse> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Login successful");
        response.setData(loginResponse);
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse refreshResponse = authenticationService.refreshToken(request);
        ApiResponse<RefreshTokenResponse> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Token refreshed successfully");
        response.setData(refreshResponse);
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authenticationService.logout(request.getRefreshToken());
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Logged out successfully");
        response.setData(null);
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChangePasswordResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        ChangePasswordResponse passwordResponse = new ChangePasswordResponse();
        passwordResponse.setMessage("Password changed successfully");
        ApiResponse<ChangePasswordResponse> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Password updated successfully");
        response.setData(passwordResponse);
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> currentUser() {
        UserResponse user = userService.getCurrentUser();
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("User details fetched successfully");
        response.setData(user);
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}

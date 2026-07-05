package com.pulse.survey.controller;

import com.pulse.survey.dto.ApiResponse;
import com.pulse.survey.entity.User;
import com.pulse.survey.enums.Role;
import com.pulse.survey.repository.UserRepository;
import com.pulse.survey.security.JwtTokenProvider;
import com.pulse.survey.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication Controller", description = "Endpoints for login and HR-driven registration")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsernameIgnoreCase(identifier);
        }

        if (userOpt.isEmpty()) {
            throw new BadRequestException("Invalid email, username, or password.");
        }

        User user = userOpt.get();
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BadRequestException("Account registration is not complete. Please create your password first.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email, username, or password.");
        }

        String token = tokenProvider.generateToken(
                user.getId().toString(),
                user.getUsername() != null ? user.getUsername() : user.getEmail(),
                user.getRole().name(),
                user.getLocation()
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername() != null ? user.getUsername() : user.getEmail(),
                "email", user.getEmail(),
                "name", user.getName(),
                "role", user.getRole().name(),
                "location", user.getLocation(),
                "status", user.getStatus()
        ));

        return ResponseEntity.ok(ApiResponse.success(responseData, "Login successful"));
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "HR registers a new employee email (pre-registration)")
    public ResponseEntity<ApiResponse<UserDto>> registerEmployee(@RequestBody RegisterEmployeeRequest request) {
        log.info("HR is registering a new employee email: {}", request.getEmail());
        
        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(request.getEmail());
        if (existingUser.isPresent()) {
            throw new BadRequestException("User with this email already exists.");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .name(request.getName().trim())
                .role(Role.EMPLOYEE)
                .location(request.getLocation().trim())
                .status("PENDING_ONBOARDING") // Require onboarding survey and password completion
                .build();

        User saved = userRepository.save(user);

        UserDto dto = new UserDto(saved.getId(), saved.getEmail(), saved.getName(), saved.getRole().name(), saved.getLocation(), saved.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Employee registered successfully. They can now complete password setup."));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify if an email is pre-registered and pending signup")
    public ResponseEntity<ApiResponse<UserDto>> verifyEmail(@RequestParam String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new BadRequestException("This email is not registered. Please contact your HR."));

        if (!"PENDING_ONBOARDING".equals(user.getStatus())) {
            throw new BadRequestException("This account is already registered. Please sign in instead.");
        }

        UserDto dto = new UserDto(user.getId(), user.getEmail(), user.getName(), user.getRole().name(), user.getLocation(), user.getStatus());
        return ResponseEntity.ok(ApiResponse.success(dto, "Email verified. Proceed to password creation."));
    }

    @PostMapping("/complete-registration")
    @Operation(summary = "Employee creates password and completes registration")
    public ResponseEntity<ApiResponse<Map<String, Object>>> completeRegistration(@RequestBody CompleteRegistrationRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new BadRequestException("Invalid email. Please check your credentials."));

        if (!"PENDING_ONBOARDING".equals(user.getStatus())) {
            throw new BadRequestException("Account registration is already complete.");
        }

        user.setUsername(request.getEmail().split("@")[0].toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Note: they are still PENDING_ONBOARDING until they complete the onboarding survey!
        User saved = userRepository.save(user);

        // Auto-login after password setup
        String token = tokenProvider.generateToken(
                saved.getId().toString(),
                saved.getUsername(),
                saved.getRole().name(),
                saved.getLocation()
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("user", Map.of(
                "id", saved.getId(),
                "username", saved.getUsername(),
                "email", saved.getEmail(),
                "name", saved.getName(),
                "role", saved.getRole().name(),
                "location", saved.getLocation(),
                "status", saved.getStatus()
        ));

        return ResponseEntity.ok(ApiResponse.success(responseData, "Account password configured and logged in."));
    }

    @PostMapping("/complete-onboarding")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Mark employee onboarding survey completed and activate status")
    public ResponseEntity<ApiResponse<String>> completeOnboarding(@RequestParam String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new BadRequestException("User not found."));

        user.setStatus("ACTIVE");
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Onboarding survey completed. Account is fully active."));
    }

    @Data
    public static class LoginRequest {
        private String identifier; // email or username
        private String password;
    }

    @Data
    public static class RegisterEmployeeRequest {
        private String email;
        private String name;
        private String location;
    }

    @Data
    public static class CompleteRegistrationRequest {
        private String email;
        private String password;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDto {
        private Long id;
        private String email;
        private String name;
        private String role;
        private String location;
        private String status;
    }
}

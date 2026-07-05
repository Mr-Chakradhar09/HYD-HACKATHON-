package com.virtusa.userservice.controller;

import com.virtusa.userservice.common.ApiResponse;
import com.virtusa.userservice.dto.request.CreateUserRequestDto;
import com.virtusa.userservice.dto.request.UpdateUserRequestDto;
import com.virtusa.userservice.dto.response.UserResponseDto;
import com.virtusa.userservice.enums.Location;
import com.virtusa.userservice.enums.Role;
import com.virtusa.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Controller", description = "Endpoints for managing user profiles (Employees, HR, and Global HR)")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Onboard a new user profile", description = "Onboards a new Employee, HR, or Global HR user. Access depends on X-User-Role and X-User-Location headers.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "21", description = "User created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payload or validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized action"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User with duplicate email or employeeId already exists")
    })
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody CreateUserRequestDto request,
            @Parameter(description = "Role of the caller making the request", required = true)
            @RequestHeader("X-User-Role") Role callerRole,
            @Parameter(description = "Location of the caller making the request (required if HR)", required = false)
            @RequestHeader(value = "X-User-Location", required = false) Location callerLocation) {
        
        log.info("REST request to create user. Employee ID: {}, Caller: {}", request.getEmployeeId(), callerRole);
        UserResponseDto createdUser = userService.createEmployee(request, callerRole, callerLocation);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User profile created successfully", createdUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by Database ID", description = "Retrieves a user profile matching the primary key database ID.")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        log.info("REST request to fetch user by database ID: {}", id);
        UserResponseDto user = userService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", user));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get user profile by unique Employee ID", description = "Retrieves a user profile matching the enterprise unique Employee ID.")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByEmployeeId(@PathVariable String employeeId) {
        log.info("REST request to fetch user by employee ID: {}", employeeId);
        UserResponseDto user = userService.getEmployeeByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", user));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user profile by unique email address", description = "Retrieves a user profile matching the unique corporate email address.")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByEmail(@PathVariable String email) {
        log.info("REST request to fetch user by email: {}", email);
        UserResponseDto user = userService.getEmployeeByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", user));
    }

    @GetMapping("/location/{location}")
    @Operation(summary = "Get user profiles by Location", description = "Retrieves list of user profiles matching a specific Location.")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getUsersByLocation(@PathVariable Location location) {
        log.info("REST request to fetch users by location: {}", location);
        List<UserResponseDto> users = userService.getEmployeesByLocation(location);
        return ResponseEntity.ok(ApiResponse.success("User profiles matching location retrieved successfully", users));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get user profiles by Role", description = "Retrieves list of user profiles matching a specific system role.")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getUsersByRole(@PathVariable Role role) {
        log.info("REST request to fetch users by role: {}", role);
        List<UserResponseDto> users = userService.getEmployeesByRole(role);
        return ResponseEntity.ok(ApiResponse.success("User profiles matching role retrieved successfully", users));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile details", description = "Updates profile fields for a user. Access check is executed based on X-User-Role and X-User-Location headers.")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDto request,
            @Parameter(description = "Role of the caller making the request", required = true)
            @RequestHeader("X-User-Role") Role callerRole,
            @Parameter(description = "Location of the caller making the request (required if HR)", required = false)
            @RequestHeader(value = "X-User-Location", required = false) Location callerLocation) {

        log.info("REST request to update user database ID: {}, Caller: {}", id, callerRole);
        UserResponseDto updatedUser = userService.updateEmployee(id, request, callerRole, callerLocation);
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", updatedUser));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user profile", description = "Deactivates a user profile by setting active status to false. Access check is executed based on X-User-Role and X-User-Location headers.")
    public ResponseEntity<ApiResponse<UserResponseDto>> deactivateUser(
            @PathVariable Long id,
            @Parameter(description = "Role of the caller making the request", required = true)
            @RequestHeader("X-User-Role") Role callerRole,
            @Parameter(description = "Location of the caller making the request (required if HR)", required = false)
            @RequestHeader(value = "X-User-Location", required = false) Location callerLocation) {

        log.info("REST request to deactivate user database ID: {}, Caller: {}", id, callerRole);
        UserResponseDto deactivatedUser = userService.deactivateEmployee(id, callerRole, callerLocation);
        return ResponseEntity.ok(ApiResponse.success("User profile deactivated successfully", deactivatedUser));
    }
}

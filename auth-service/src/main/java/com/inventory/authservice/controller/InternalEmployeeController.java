package com.inventory.authservice.controller;

import com.inventory.authservice.dto.response.ApiResponse;
import com.inventory.authservice.dto.response.InternalEmployeeResponse;
import com.inventory.authservice.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/employees")
public class InternalEmployeeController {
    private final EmployeeService employeeService;
    public InternalEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InternalEmployeeResponse>>> getAllEmployees(
            HttpServletRequest request) {

        List<InternalEmployeeResponse> employees = employeeService.getAllEmployees().stream()
                .map(e -> new InternalEmployeeResponse(
                        e.getId(),
                        e.getEmployeeCode(),
                        e.getFullName(),
                        e.getEmail(),
                        e.getStatus(),
                        e.getRoles() != null ? new java.util.HashSet<>(e.getRoles()) : java.util.Set.of()
                ))
                .collect(Collectors.toList());

        ApiResponse<List<InternalEmployeeResponse>> apiResponse =
                new ApiResponse<>(
                        true,
                        "Employees retrieved successfully",
                        employees,
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        HttpStatus.OK.value()
                );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<InternalEmployeeResponse>> getEmployeeById(
            @PathVariable Long employeeId,
            HttpServletRequest request) {

        InternalEmployeeResponse response = employeeService.getInternalEmployeeById(employeeId);
        ApiResponse<InternalEmployeeResponse> apiResponse =
                new ApiResponse<>(
                        true,
                        "Employee retrieved successfully",
                        response,
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        HttpStatus.OK.value()
                );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/code/{employeeCode}")
    public ResponseEntity<ApiResponse<InternalEmployeeResponse>> getEmployeeByCode(
            @PathVariable String employeeCode,
            HttpServletRequest request) {

        InternalEmployeeResponse response = employeeService.getInternalEmployeeByCode(employeeCode);
        ApiResponse<InternalEmployeeResponse> apiResponse =
                new ApiResponse<>(
                        true,
                        "Employee retrieved successfully",
                        response,
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        HttpStatus.OK.value()
                );

        return ResponseEntity.ok(apiResponse);
    }
}
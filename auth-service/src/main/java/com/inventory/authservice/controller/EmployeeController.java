package com.inventory.authservice.controller;

import com.inventory.authservice.dto.request.CreateEmployeeRequest;
import com.inventory.authservice.dto.request.UpdateEmployeeRequest;
import com.inventory.authservice.dto.response.ApiResponse;
import com.inventory.authservice.dto.response.CreateEmployeeResponse;
import com.inventory.authservice.dto.response.EmployeeResponse;
import com.inventory.authservice.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/employees")
@Validated
public class EmployeeController {
    private final EmployeeService employeeService;
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<CreateEmployeeResponse>> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        CreateEmployeeResponse employee = employeeService.createEmployee(request);
        ApiResponse<CreateEmployeeResponse> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("Employee created successfully");
        response.setData(employee);
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeByCode(@PathVariable String employeeCode) {
        EmployeeResponse employee = employeeService.getEmployeeByCode(employeeCode);
        ApiResponse<EmployeeResponse> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Employee fetched successfully");
        response.setData(employee);
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        ApiResponse<List<EmployeeResponse>> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Employees fetched successfully");
        response.setData(employees);
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{employeeCode}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(@PathVariable String employeeCode, @Valid @RequestBody UpdateEmployeeRequest request) {
        EmployeeResponse employee = employeeService.updateEmployee(employeeCode, request);
        ApiResponse<EmployeeResponse> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Employee updated successfully");
        response.setData(employee);
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{employeeCode}/activate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateEmployee(@PathVariable String employeeCode) {
        employeeService.activateEmployee(employeeCode);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Employee activated successfully");
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{employeeCode}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateEmployee(@PathVariable String employeeCode) {
        employeeService.deactivateEmployee(employeeCode);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Employee deactivated successfully");
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{employeeCode}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable String employeeCode) {
        employeeService.deleteEmployee(employeeCode);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Employee deleted successfully");
        response.setTimestamp(LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
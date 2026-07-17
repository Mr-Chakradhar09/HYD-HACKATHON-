package com.inventory.warehouseservice.controller;

import com.inventory.warehouseservice.dto.request.AssignEmployeeRequest;
import com.inventory.warehouseservice.dto.request.UpdateAssignmentRequest;
import com.inventory.warehouseservice.dto.response.ApiResponse;
import com.inventory.warehouseservice.dto.response.InternalEmployeeResponse;
import com.inventory.warehouseservice.dto.response.WarehouseAssignmentResponse;
import com.inventory.warehouseservice.service.interfaces.WarehouseAssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class WarehouseAssignmentController {

    private final WarehouseAssignmentService assignmentService;

    public WarehouseAssignmentController(WarehouseAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/warehouses/unassigned-employees")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<List<InternalEmployeeResponse>>> getUnassignedEmployees(
            HttpServletRequest servletRequest) {

        List<InternalEmployeeResponse> response = assignmentService.getUnassignedEmployees();

        ApiResponse<List<InternalEmployeeResponse>> apiResponse = new ApiResponse<>(
                true,
                "Unassigned employees retrieved successfully.",
                response,
                LocalDateTime.now(),
                servletRequest.getRequestURI(),
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/warehouses/{warehouseId}/assignments")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<WarehouseAssignmentResponse>> assignEmployee(
            @PathVariable Long warehouseId,
            @Valid @RequestBody AssignEmployeeRequest request,
            HttpServletRequest servletRequest) {

        WarehouseAssignmentResponse response = assignmentService.assignEmployee(warehouseId, request);

        ApiResponse<WarehouseAssignmentResponse> apiResponse = new ApiResponse<>(
                true,
                "Employee assigned to warehouse successfully.",
                response,
                LocalDateTime.now(),
                servletRequest.getRequestURI(),
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/warehouses/{warehouseId}/assignments")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','INVENTORY_MANAGER','PROCUREMENT_MANAGER','WAREHOUSE_OPERATOR')")
    public ResponseEntity<ApiResponse<Page<WarehouseAssignmentResponse>>> getAssignmentsByWarehouse(
            @PathVariable Long warehouseId,
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request) {

        Page<WarehouseAssignmentResponse> response = assignmentService.getAssignmentsByWarehouse(warehouseId, pageable);

        ApiResponse<Page<WarehouseAssignmentResponse>> apiResponse = new ApiResponse<>(
                true,
                "Assignments retrieved successfully.",
                response,
                LocalDateTime.now(),
                request.getRequestURI(),
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<WarehouseAssignmentResponse>> updateAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody UpdateAssignmentRequest request,
            HttpServletRequest servletRequest) {

        WarehouseAssignmentResponse response = assignmentService.updateAssignment(assignmentId, request);

        ApiResponse<WarehouseAssignmentResponse> apiResponse = new ApiResponse<>(
                true,
                "Assignment updated successfully.",
                response,
                LocalDateTime.now(),
                servletRequest.getRequestURI(),
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/assignments/{assignmentId}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deactivateAssignment(
            @PathVariable Long assignmentId,
            HttpServletRequest request) {

        assignmentService.deactivateAssignment(assignmentId);

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                true,
                "Assignment deactivated successfully.",
                null,
                LocalDateTime.now(),
                request.getRequestURI(),
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/warehouses/my-assignment")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_OPERATOR', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<WarehouseAssignmentResponse>> getMyAssignment(
            HttpServletRequest servletRequest) {

        WarehouseAssignmentResponse response = assignmentService.getMyAssignment();

        ApiResponse<WarehouseAssignmentResponse> apiResponse = new ApiResponse<>(
                true,
                response != null ? "Assignment retrieved successfully." : "No active assignment found.",
                response,
                LocalDateTime.now(),
                servletRequest.getRequestURI(),
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(apiResponse);
    }
}

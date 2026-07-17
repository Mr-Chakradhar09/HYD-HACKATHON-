package com.inventory.warehouseservice.controller;

import com.inventory.warehouseservice.dto.response.ApiResponse;
import com.inventory.warehouseservice.dto.response.WarehouseAssignmentResponse;
import com.inventory.warehouseservice.dto.response.WarehouseResponse;
import com.inventory.warehouseservice.service.interfaces.WarehouseAssignmentService;
import com.inventory.warehouseservice.service.interfaces.WarehouseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/internal/warehouses")
public class InternalWarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseAssignmentService assignmentService;

    public InternalWarehouseController(WarehouseService warehouseService, WarehouseAssignmentService assignmentService) {
        this.warehouseService = warehouseService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseById(
            @PathVariable Long warehouseId,
            HttpServletRequest request) {
            
        WarehouseResponse response = warehouseService.getWarehouseById(warehouseId);
        
        ApiResponse<WarehouseResponse> apiResponse = new ApiResponse<>(
                true,
                "Warehouse retrieved successfully.",
                response,
                LocalDateTime.now(),
                request.getRequestURI(),
                HttpStatus.OK.value()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{warehouseId}/assignments")
    public ResponseEntity<ApiResponse<Page<WarehouseAssignmentResponse>>> getAssignmentsByWarehouse(
            @PathVariable Long warehouseId,
            HttpServletRequest request) {

        Page<WarehouseAssignmentResponse> response = assignmentService.getAssignmentsByWarehouse(
                warehouseId, PageRequest.of(0, 100));

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
}

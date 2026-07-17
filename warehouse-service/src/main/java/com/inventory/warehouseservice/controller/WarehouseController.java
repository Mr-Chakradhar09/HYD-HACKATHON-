package com.inventory.warehouseservice.controller;

import com.inventory.warehouseservice.dto.request.CreateWarehouseRequest;
import com.inventory.warehouseservice.dto.request.UpdateWarehouseRequest;
import com.inventory.warehouseservice.dto.response.ApiResponse;
import com.inventory.warehouseservice.dto.response.WarehouseResponse;
import com.inventory.warehouseservice.dto.response.WarehouseSummaryResponse;
import com.inventory.warehouseservice.enums.WarehouseStatus;
import com.inventory.warehouseservice.service.interfaces.WarehouseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request, HttpServletRequest servletRequest) {
        WarehouseResponse response = warehouseService.createWarehouse(request);
        ApiResponse<WarehouseResponse> apiResponse =
                new ApiResponse<>(
                        true,
                        "Warehouse created successfully.",
                        response,
                        LocalDateTime.now(),
                        servletRequest.getRequestURI(),
                        HttpStatus.CREATED.value()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
    @GetMapping("/{warehouseId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','INVENTORY_MANAGER','PROCUREMENT_MANAGER','WAREHOUSE_OPERATOR')")
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
    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','INVENTORY_MANAGER','PROCUREMENT_MANAGER','WAREHOUSE_OPERATOR')")
    public ResponseEntity<ApiResponse<Page<WarehouseSummaryResponse>>> getAllWarehouses(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "warehouseName",
                    direction = Sort.Direction.ASC
            ) Pageable pageable,
            HttpServletRequest request) {
        Page<WarehouseSummaryResponse> response = warehouseService.getAllWarehouses(pageable);
        ApiResponse<Page<WarehouseSummaryResponse>> apiResponse =
                new ApiResponse<>(
                        true,
                        "Warehouses retrieved successfully.",
                        response,
                        LocalDateTime.now(),
                        request.getRequestURI(),
                        HttpStatus.OK.value()
                );

        return ResponseEntity.ok(apiResponse);
    }
    @PutMapping("/{warehouseId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @PathVariable Long warehouseId,
            @Valid @RequestBody UpdateWarehouseRequest request,
            HttpServletRequest servletRequest) {

        WarehouseResponse response =
                warehouseService.updateWarehouse(warehouseId, request);

        ApiResponse<WarehouseResponse> apiResponse =
                new ApiResponse<>(
                        true,
                        "Warehouse updated successfully.",
                        response,
                        LocalDateTime.now(),
                        servletRequest.getRequestURI(),
                        HttpStatus.OK.value()
                );

        return ResponseEntity.ok(apiResponse);
    }
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','INVENTORY_MANAGER','PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<Page<WarehouseSummaryResponse>>> searchWarehouses(@RequestParam(required = false) String keyword, @RequestParam(required = false) WarehouseStatus status,
            @PageableDefault(
                    size = 10,
                    sort = "warehouseName")
            Pageable pageable, HttpServletRequest request) {
        Page<WarehouseSummaryResponse> response = warehouseService.searchWarehouses(keyword, status, pageable);

        ApiResponse<Page<WarehouseSummaryResponse>> apiResponse =
                new ApiResponse<>(

                        true,

                        "Warehouses retrieved successfully.",

                        response,

                        LocalDateTime.now(),

                        request.getRequestURI(),

                        HttpStatus.OK.value());

        return ResponseEntity.ok(apiResponse);
    }
    @PatchMapping("/{warehouseId}/activate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateWarehouse(@PathVariable Long warehouseId, HttpServletRequest request) {
        warehouseService.activateWarehouse(warehouseId);
        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "Warehouse activated successfully.",
                null,
                LocalDateTime.now(),
                request.getRequestURI(),
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{warehouseId}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateWarehouse(@PathVariable Long warehouseId, HttpServletRequest request) {
        warehouseService.deactivateWarehouse(warehouseId);
        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "Warehouse deactivated successfully.",
                null,
                LocalDateTime.now(),
                request.getRequestURI(),
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{warehouseId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable Long warehouseId, HttpServletRequest request) {
        warehouseService.deleteWarehouse(warehouseId);
        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "Warehouse deleted successfully.",
                null,
                LocalDateTime.now(),
                request.getRequestURI(),
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }
}

package com.inventory.movementservice.controller;

import com.inventory.movementservice.dto.request.CreateMovementRequest;
import com.inventory.movementservice.dto.response.ApiResponse;
import com.inventory.movementservice.entity.InventoryMovement;
import com.inventory.movementservice.service.MovementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movements")
public class MovementController {

    private final MovementService movementService;

    public MovementController(MovementService movementService) { this.movementService = movementService; }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_OPERATOR', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryMovement>> createMovement(
            @Valid @RequestBody CreateMovementRequest request, Authentication authentication) {
        InventoryMovement movement = movementService.createMovement(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Movement created successfully", movement, HttpStatus.CREATED.value()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InventoryMovement>> getMovementById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Movement fetched", movementService.getMovementById(id), HttpStatus.OK.value()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<InventoryMovement>>> getAllMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(new ApiResponse<>(true, "Movements fetched", movementService.getAllMovements(pageable), HttpStatus.OK.value()));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<InventoryMovement>>> searchMovements(
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(new ApiResponse<>(true, "Movements found", movementService.searchMovements(movementType, status, productId, warehouseId, pageable), HttpStatus.OK.value()));
    }
}

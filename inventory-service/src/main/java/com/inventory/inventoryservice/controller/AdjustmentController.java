package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.request.CreateAdjustmentRequest;
import com.inventory.inventoryservice.dto.response.ApiResponse;
import com.inventory.inventoryservice.entity.InventoryAdjustment;
import com.inventory.inventoryservice.service.AdjustmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/adjustments")
public class AdjustmentController {

    private final AdjustmentService adjustmentService;

    public AdjustmentController(AdjustmentService adjustmentService) { this.adjustmentService = adjustmentService; }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryAdjustment>> createAdjustment(
            @Valid @RequestBody CreateAdjustmentRequest request, Authentication authentication) {
        InventoryAdjustment adjustment = adjustmentService.createAdjustment(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Adjustment created", adjustment, HttpStatus.CREATED.value()));
    }
}

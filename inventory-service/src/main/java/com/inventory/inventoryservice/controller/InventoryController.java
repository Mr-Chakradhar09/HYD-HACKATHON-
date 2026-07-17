package com.inventory.inventoryservice.controller;

import com.inventory.inventoryservice.dto.request.CreateInventoryRequest;
import com.inventory.inventoryservice.dto.response.ApiResponse;
import com.inventory.inventoryservice.dto.response.InventoryResponse;
import com.inventory.inventoryservice.dto.response.TransferRecommendationResponse;
import com.inventory.inventoryservice.enums.InventoryStatus;
import com.inventory.inventoryservice.service.InventoryService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService; }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
            @Valid @RequestBody CreateInventoryRequest request, Authentication authentication) {
        InventoryResponse inventory = inventoryService.createInventory(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Inventory created successfully", inventory, HttpStatus.CREATED.value()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryById(@PathVariable Long id) {
        InventoryResponse inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory fetched", inventory, HttpStatus.OK.value()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>> getAllInventories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventories fetched", inventoryService.getAllInventories(pageable), HttpStatus.OK.value()));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>> searchInventories(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) InventoryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("productId").ascending());
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventories found", inventoryService.searchInventories(search, warehouseId, productId, status, pageable), HttpStatus.OK.value()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deactivateInventory(@PathVariable Long id, Authentication authentication) {
        inventoryService.deactivateInventory(id, authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory deactivated", null, HttpStatus.OK.value()));
    }

    @GetMapping("/transfer-recommendations")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'INVENTORY_MANAGER', 'PROCUREMENT_MANAGER')")
    public ResponseEntity<ApiResponse<List<TransferRecommendationResponse>>> getTransferRecommendations(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        List<TransferRecommendationResponse> recommendations = inventoryService.getTransferRecommendations(productId, quantity);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transfer recommendations fetched", recommendations, HttpStatus.OK.value()));
    }
}

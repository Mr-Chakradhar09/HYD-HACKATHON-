package com.inventory.replenishment.controller;

import com.inventory.replenishment.dto.PurchaseRequestRequest;
import com.inventory.replenishment.dto.PurchaseRequestResponse;
import com.inventory.replenishment.service.ReplenishmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-requests")
public class PurchaseRequestController {

    private final ReplenishmentService replenishmentService;

    public PurchaseRequestController(ReplenishmentService replenishmentService) {
        this.replenishmentService = replenishmentService;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseRequestResponse>> getAll(
            @RequestParam(required = false) Long warehouseId) {
        if (warehouseId != null) {
            return ResponseEntity.ok(replenishmentService.getPurchaseRequestsByWarehouse(warehouseId));
        }
        return ResponseEntity.ok(replenishmentService.getAllPurchaseRequests());
    }

    @PostMapping
    public ResponseEntity<PurchaseRequestResponse> create(@Valid @RequestBody PurchaseRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(replenishmentService.createPurchaseRequest(request));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<PurchaseRequestResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(replenishmentService.approvePurchaseRequest(id));
    }
}

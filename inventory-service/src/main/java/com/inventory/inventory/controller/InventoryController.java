package com.inventory.inventory.controller;

import com.inventory.inventory.dto.InventoryRequest;
import com.inventory.inventory.dto.InventoryResponse;
import com.inventory.inventory.dto.ReservationRequest;
import com.inventory.inventory.dto.ReservationResponse;
import com.inventory.inventory.dto.TransferRequest;
import com.inventory.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/inbound")
    public ResponseEntity<InventoryResponse> inbound(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.inbound(request));
    }

    @PostMapping("/outbound")
    public ResponseEntity<InventoryResponse> outbound(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.outbound(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        inventoryService.transfer(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/adjustment")
    public ResponseEntity<InventoryResponse> adjustment(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.adjustment(request));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory(
            @RequestParam(required = false) Long warehouseId) {
        if (warehouseId != null) {
            return ResponseEntity.ok(inventoryService.getInventoryByWarehouse(warehouseId));
        }
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<List<InventoryResponse>> getInventoryByProduct(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryByProduct(id));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(inventoryService.reserve(request));
    }

    @PostMapping("/release/{id}")
    public ResponseEntity<Void> release(@PathVariable Long id) {
        inventoryService.release(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release-by-order/{orderId}")
    public ResponseEntity<Void> releaseByOrderId(@PathVariable String orderId) {
        inventoryService.releaseByOrderId(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ship/{id}")
    public ResponseEntity<Void> ship(@PathVariable Long id) {
        inventoryService.ship(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(inventoryService.getAllReservations());
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getReservation(id));
    }

    @GetMapping("/reservations/order/{orderId}")
    public ResponseEntity<List<ReservationResponse>> getReservationsByOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(inventoryService.getReservationsByOrder(orderId));
    }
}

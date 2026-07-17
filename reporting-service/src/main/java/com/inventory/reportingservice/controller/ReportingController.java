package com.inventory.reportingservice.controller;

import com.inventory.reportingservice.dto.response.MovementHistoryResponse;
import com.inventory.reportingservice.service.interfaces.ReportingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {

    private final ReportingService service;

    public ReportingController(ReportingService service) {
        this.service = service;
    }

    @GetMapping("/movements")
    public ResponseEntity<Page<MovementHistoryResponse>> getMovements(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            Pageable pageable) {
        if (productId != null) {
            return ResponseEntity.ok(service.getMovementsByProductId(productId, pageable));
        } else if (warehouseId != null) {
            return ResponseEntity.ok(service.getMovementsByWarehouseId(warehouseId, pageable));
        }
        return ResponseEntity.ok(service.getAllMovements(pageable));
    }

    @GetMapping("/inventory-summary")
    public ResponseEntity<Object> getInventorySummary(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getInventorySummary(warehouseId, productId, page, size));
    }
}

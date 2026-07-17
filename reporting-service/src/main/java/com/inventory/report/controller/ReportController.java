package com.inventory.report.controller;

import com.inventory.report.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<Map<String, Object>>> getInventoryReport() {
        return ResponseEntity.ok(reportService.getInventoryReport());
    }

    @GetMapping("/movement")
    public ResponseEntity<List<Map<String, Object>>> getMovementReport() {
        return ResponseEntity.ok(reportService.getMovementReport());
    }

    @GetMapping("/warehouse")
    public ResponseEntity<List<Map<String, Object>>> getWarehouseReport() {
        return ResponseEntity.ok(reportService.getWarehouseReport());
    }
}

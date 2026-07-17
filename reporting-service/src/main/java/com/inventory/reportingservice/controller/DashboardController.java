package com.inventory.reportingservice.controller;

import com.inventory.reportingservice.dto.response.ApiResponse;
import com.inventory.reportingservice.dto.response.DashboardSummary;
import com.inventory.reportingservice.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) { this.dashboardService = dashboardService; }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardSummary>> getSummary() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard summary", dashboardService.getSummary(), HttpStatus.OK.value()));
    }
}

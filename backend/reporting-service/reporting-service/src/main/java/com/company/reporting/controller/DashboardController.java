package com.company.reporting.controller;

import com.company.reporting.dto.DashboardResponseDTO;
import com.company.reporting.dto.LocationReportDTO;
import com.company.reporting.dto.ThemeReportDTO;
import com.company.reporting.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard API", description = "Endpoints for employee sentiment dashboards")
public class DashboardController {

    private final DashboardService dashboardService;

    private String getMonthOrDefault(String month) {
        if (month == null || month.trim().isEmpty()) {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return month;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get overall organization sentiment overview")
    public ResponseEntity<DashboardResponseDTO> getOverviewDashboard(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(dashboardService.getOverviewDashboard(getMonthOrDefault(month)));
    }

    @GetMapping("/locations")
    @Operation(summary = "Get location-wise pulse scores")
    public ResponseEntity<List<LocationReportDTO>> getLocationDashboard(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(dashboardService.getLocationDashboard(getMonthOrDefault(month)));
    }

    @GetMapping("/themes")
    @Operation(summary = "Get theme-wise pulse scores")
    public ResponseEntity<List<ThemeReportDTO>> getThemeDashboard(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(dashboardService.getThemeDashboard(getMonthOrDefault(month)));
    }

    @GetMapping("/departments")
    @Operation(summary = "Get department-wise pulse scores")
    public ResponseEntity<List<LocationReportDTO>> getDepartmentDashboard(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(dashboardService.getDepartmentDashboard(getMonthOrDefault(month)));
    }
}

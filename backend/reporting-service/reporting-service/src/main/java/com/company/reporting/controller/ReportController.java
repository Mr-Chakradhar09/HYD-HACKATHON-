package com.company.reporting.controller;

import com.company.reporting.dto.LocationReportDTO;
import com.company.reporting.entity.SurveySnapshot;
import com.company.reporting.service.AnalyticsService;
import com.company.reporting.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "Endpoints for detailed reporting and data ingestion")
public class ReportController {

    private final ReportService reportService;
    private final AnalyticsService analyticsService;

    private String getMonthOrDefault(String month) {
        if (month == null || month.trim().isEmpty()) {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return month;
    }

    @GetMapping("/location/{locationType}")
    @Operation(summary = "Get reports by location type (CITY, STATE, COUNTRY, REGION)")
    public ResponseEntity<List<LocationReportDTO>> getReportsByType(
            @PathVariable String locationType,
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(reportService.getReportsByType(locationType, getMonthOrDefault(month)));
    }

    @GetMapping("/location/{locationType}/{locationName}")
    @Operation(summary = "Get specific location report by name and type")
    public ResponseEntity<LocationReportDTO> getReportByNameAndType(
            @PathVariable String locationType,
            @PathVariable String locationName,
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(reportService.getReportByNameAndType(locationName, locationType, getMonthOrDefault(month)));
    }

    @GetMapping("/survey/{surveyId}")
    @Operation(summary = "Get raw snapshots for a specific survey ID")
    public ResponseEntity<List<SurveySnapshot>> getRawSnapshotsBySurvey(@PathVariable Long surveyId) {
        return ResponseEntity.ok(reportService.getRawSnapshotsBySurvey(surveyId));
    }

    @PostMapping("/ingest")
    @Operation(summary = "Ingest a new survey feedback response record")
    public ResponseEntity<String> ingestResponse(@RequestBody SurveySnapshot snapshot) {
        if (snapshot.getMonth() == null || snapshot.getMonth().trim().isEmpty()) {
            snapshot.setMonth(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        analyticsService.processSurveySubmission(snapshot);
        return ResponseEntity.ok("Successfully processed response and refreshed aggregates.");
    }

    @PostMapping("/aggregate")
    @Operation(summary = "Force compilation of aggregates for a given month")
    public ResponseEntity<String> triggerAggregation(@RequestParam(required = false) String month) {
        String targetMonth = getMonthOrDefault(month);
        analyticsService.aggregateMonthlyData(targetMonth);
        return ResponseEntity.ok("Aggregates successfully recompiled for " + targetMonth);
    }
}

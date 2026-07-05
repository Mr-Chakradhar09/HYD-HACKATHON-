package com.company.reporting.controller;

import com.company.reporting.dto.AIInsightDTO;
import com.company.reporting.service.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports/insights")
@RequiredArgsConstructor
@Tag(name = "AI Insights API", description = "Endpoints for AI-driven sentiment analysis summaries and concern hotspots")
public class InsightController {

    private final InsightService insightService;

    private String getMonthOrDefault(String month) {
        if (month == null || month.trim().isEmpty()) {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return month;
    }

    @GetMapping
    @Operation(summary = "Get complete AI insights object for a month")
    public ResponseEntity<AIInsightDTO> getInsightByMonth(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(insightService.getInsightByMonth(getMonthOrDefault(month)));
    }

    @GetMapping("/top-concerns")
    @Operation(summary = "Get list of top concern areas for a month")
    public ResponseEntity<List<String>> getTopConcerns(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(insightService.getTopConcerns(getMonthOrDefault(month)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get sentiment summary narrative for a month")
    public ResponseEntity<String> getSentimentSummary(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(insightService.getSentimentSummary(getMonthOrDefault(month)));
    }

    @GetMapping("/location")
    @Operation(summary = "Get location specific AI analysis for a month")
    public ResponseEntity<String> getLocationAnalysis(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(insightService.getLocationAnalysis(getMonthOrDefault(month)));
    }
}

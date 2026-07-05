package com.company.reporting.controller;

import com.company.reporting.dto.TrendDTO;
import com.company.reporting.service.TrendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/trends")
@RequiredArgsConstructor
@Tag(name = "Trend API", description = "Endpoints for employee sentiment historical trends")
public class TrendController {

    private final TrendService trendService;

    @GetMapping("/organization")
    @Operation(summary = "Get historical trends for the entire organization")
    public ResponseEntity<List<TrendDTO>> getOrganizationTrends() {
        return ResponseEntity.ok(trendService.getOrganizationTrends());
    }

    @GetMapping("/location/{location}")
    @Operation(summary = "Get historical trends for a specific location")
    public ResponseEntity<List<TrendDTO>> getLocationTrends(@PathVariable String location) {
        return ResponseEntity.ok(trendService.getLocationTrends(location));
    }

    @GetMapping("/theme/{theme}")
    @Operation(summary = "Get historical trends for a specific theme")
    public ResponseEntity<List<TrendDTO>> getThemeTrends(@PathVariable String theme) {
        return ResponseEntity.ok(trendService.getThemeTrends(theme));
    }
}

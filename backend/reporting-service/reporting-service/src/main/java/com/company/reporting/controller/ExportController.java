package com.company.reporting.controller;

import com.company.reporting.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports/export")
@RequiredArgsConstructor
@Tag(name = "Export API", description = "Endpoints for downloading reports in PDF and CSV format")
public class ExportController {

    private final ExportService exportService;

    private String getMonthOrDefault(String month) {
        if (month == null || month.trim().isEmpty()) {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return month;
    }

    @GetMapping("/pdf")
    @Operation(summary = "Export monthly pulse survey report as PDF")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) String month) {
        String targetMonth = getMonthOrDefault(month);
        byte[] pdfBytes = exportService.exportPdf(targetMonth);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "pulse-report-" + targetMonth + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/csv")
    @Operation(summary = "Export monthly pulse survey report as CSV")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) String month) {
        String targetMonth = getMonthOrDefault(month);
        byte[] csvBytes = exportService.exportCsv(targetMonth);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "pulse-report-" + targetMonth + ".csv");
        headers.setContentLength(csvBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
}

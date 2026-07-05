package com.pulse.survey.controller;

import com.pulse.survey.dto.ApiResponse;
import com.pulse.survey.dto.request.SurveySubmissionRequest;
import com.pulse.survey.dto.response.SubmissionStatusResponse;
import com.pulse.survey.dto.response.SurveyReportResponse;
import com.pulse.survey.security.SecurityUtils;
import com.pulse.survey.service.ResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/responses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Response Controller", description = "Endpoints for employee responses and survey reports")
public class ResponseController {

    private final ResponseService responseService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Submit response to a survey (EMPLOYEE only)")
    public ResponseEntity<ApiResponse<SubmissionStatusResponse>> submitResponse(
            @Valid @RequestBody SurveySubmissionRequest request) {
        
        String employeeId = SecurityUtils.getCurrentUserId();
        SubmissionStatusResponse response = responseService.submitSurveyResponse(request, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Survey response submitted successfully"));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Check if employee has submitted response for a survey")
    public ResponseEntity<ApiResponse<SubmissionStatusResponse>> getSubmissionStatus(
            @RequestParam Long surveyId) {
        String employeeId = SecurityUtils.getCurrentUserId();
        SubmissionStatusResponse response = responseService.getSubmissionStatus(surveyId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(response, "Fetched submission status"));
    }

    @GetMapping("/{surveyId}")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Get survey reports and responses (HR or GLOBAL_HR)")
    public ResponseEntity<ApiResponse<SurveyReportResponse>> getSurveyReport(@PathVariable Long surveyId) {
        // First retrieve report data
        SurveyReportResponse report = responseService.generateSurveyReport(surveyId);

        // Enforce location restrictions for local HR role
        if (SecurityUtils.isCurrentUserInRole("HR") && !SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
            String hrLocation = SecurityUtils.getCurrentUserLocation();
            if (hrLocation == null || !hrLocation.equalsIgnoreCase(report.getLocation())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You are only authorized to view reports for your location: " + hrLocation));
            }
        }

        return ResponseEntity.ok(ApiResponse.success(report, "Survey report generated successfully"));
    }
}

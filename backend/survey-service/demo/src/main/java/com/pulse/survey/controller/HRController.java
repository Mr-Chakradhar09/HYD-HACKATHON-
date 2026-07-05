package com.pulse.survey.controller;

import com.pulse.survey.dto.ApiResponse;
import com.pulse.survey.dto.response.SurveyResponseDto;
import com.pulse.survey.exception.BadRequestException;
import com.pulse.survey.security.SecurityUtils;
import com.pulse.survey.service.SurveyGenerationService;
import com.pulse.survey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "HR Controller", description = "Endpoints for HR operations, including triggering AI Analysis")
public class HRController {

    private final SurveyGenerationService surveyGenerationService;
    private final SurveyService surveyService;

    @PostMapping("/{surveyId}/generate-ai-questions")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Analyze survey responses and generate AI questions (HR or GLOBAL_HR)")
    public ResponseEntity<ApiResponse<Void>> generateAiQuestions(@PathVariable Long surveyId) {
        log.info("HR request received to generate AI questions for survey ID: {}", surveyId);

        // Security check for location authority
        if (SecurityUtils.isCurrentUserInRole("HR") && !SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
            String hrLocation = SecurityUtils.getCurrentUserLocation();
            SurveyResponseDto survey = surveyService.getSurveyById(surveyId);
            if (hrLocation == null || !hrLocation.equalsIgnoreCase(survey.getLocation())) {
                throw new BadRequestException("You are not authorized to trigger AI analysis for this location.");
            }
        }

        surveyGenerationService.generateAIQuestions(surveyId);
        return ResponseEntity.ok(ApiResponse.success("AI question generation triggered successfully. Draft questions will be created."));
    }
}

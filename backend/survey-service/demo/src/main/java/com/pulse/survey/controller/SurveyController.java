package com.pulse.survey.controller;

import com.pulse.survey.dto.ApiResponse;
import com.pulse.survey.dto.request.SurveyRequest;
import com.pulse.survey.dto.response.SurveyResponseDto;
import com.pulse.survey.exception.BadRequestException;
import com.pulse.survey.security.SecurityUtils;
import com.pulse.survey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Survey Controller", description = "Endpoints for Survey lifecycle management")
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Create a monthly survey (HR or GLOBAL_HR)")
    public ResponseEntity<ApiResponse<SurveyResponseDto>> createSurvey(@Valid @RequestBody SurveyRequest request) {
        // Enforce location constraint for HR role
        if (SecurityUtils.isCurrentUserInRole("HR") && !SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
            String hrLocation = SecurityUtils.getCurrentUserLocation();
            if (hrLocation == null || !hrLocation.equalsIgnoreCase(request.getLocation())) {
                throw new BadRequestException("HR users can only create surveys for their assigned location: " + hrLocation);
            }
        }
        
        SurveyResponseDto response = surveyService.createSurvey(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Survey created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Get list of surveys (filtered by location for HR)")
    public ResponseEntity<ApiResponse<Page<SurveyResponseDto>>> getAllSurveys(
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "id,desc") String sort) {

        String filterLocation = location;
        if (SecurityUtils.isCurrentUserInRole("HR") && !SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
            filterLocation = SecurityUtils.getCurrentUserLocation();
            log.debug("Overriding survey query location filter to HR's location: {}", filterLocation);
        }

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<SurveyResponseDto> surveys = surveyService.getAllSurveys(pageable, filterLocation);
        return ResponseEntity.ok(ApiResponse.success(surveys, "Fetched surveys page successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR', 'EMPLOYEE')")
    @Operation(summary = "Get survey details by ID")
    public ResponseEntity<ApiResponse<SurveyResponseDto>> getSurveyById(@PathVariable Long id) {
        SurveyResponseDto response = surveyService.getSurveyById(id);
        
        // Enforce location checks for HR and EMPLOYEE roles
        if (!SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
            String userLocation = SecurityUtils.getCurrentUserLocation();
            if (userLocation == null || !userLocation.equalsIgnoreCase(response.getLocation())) {
                throw new BadRequestException("You do not have access to surveys from this location.");
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success(response, "Fetched survey details"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Update a monthly survey (HR or GLOBAL_HR)")
    public ResponseEntity<ApiResponse<SurveyResponseDto>> updateSurvey(
            @PathVariable Long id,
            @Valid @RequestBody SurveyRequest request) {
        
        // Validate location authority
        if (SecurityUtils.isCurrentUserInRole("HR") && !SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
            String hrLocation = SecurityUtils.getCurrentUserLocation();
            if (hrLocation == null || !hrLocation.equalsIgnoreCase(request.getLocation())) {
                throw new BadRequestException("HR users can only manage surveys for their assigned location: " + hrLocation);
            }
            
            // Validate that the existing survey belongs to the HR's location
            SurveyResponseDto existing = surveyService.getSurveyById(id);
            if (!existing.getLocation().equalsIgnoreCase(hrLocation)) {
                throw new BadRequestException("You are not authorized to update this survey.");
            }
        }

        SurveyResponseDto response = surveyService.updateSurvey(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Survey updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Delete survey (HR or GLOBAL_HR)")
    public ResponseEntity<ApiResponse<Void>> deleteSurvey(@PathVariable Long id) {
        if (SecurityUtils.isCurrentUserInRole("HR") && !SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
            String hrLocation = SecurityUtils.getCurrentUserLocation();
            SurveyResponseDto existing = surveyService.getSurveyById(id);
            if (!existing.getLocation().equalsIgnoreCase(hrLocation)) {
                throw new BadRequestException("You are not authorized to delete this survey.");
            }
        }

        surveyService.deleteSurvey(id);
        return ResponseEntity.ok(ApiResponse.success("Survey deleted successfully"));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Publish a draft survey (HR or GLOBAL_HR)")
    public ResponseEntity<ApiResponse<SurveyResponseDto>> publishSurvey(@PathVariable Long id) {
        if (SecurityUtils.isCurrentUserInRole("HR") && !SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
            String hrLocation = SecurityUtils.getCurrentUserLocation();
            SurveyResponseDto existing = surveyService.getSurveyById(id);
            if (!existing.getLocation().equalsIgnoreCase(hrLocation)) {
                throw new BadRequestException("You are not authorized to publish this survey.");
            }
        }

        SurveyResponseDto response = surveyService.publishSurvey(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Survey published successfully. Notifications sent to employees."));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Close a published survey (HR or GLOBAL_HR)")
    public ResponseEntity<ApiResponse<SurveyResponseDto>> closeSurvey(@PathVariable Long id) {
        if (SecurityUtils.isCurrentUserInRole("HR") && !SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
            String hrLocation = SecurityUtils.getCurrentUserLocation();
            SurveyResponseDto existing = surveyService.getSurveyById(id);
            if (!existing.getLocation().equalsIgnoreCase(hrLocation)) {
                throw new BadRequestException("You are not authorized to close this survey.");
            }
        }

        SurveyResponseDto response = surveyService.closeSurvey(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Survey closed successfully. Notifications sent."));
    }

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Select questions for a draft survey")
    public ResponseEntity<ApiResponse<SurveyResponseDto>> selectQuestions(
            @PathVariable Long id,
            @RequestBody List<Long> questionIds) {
        
        if (SecurityUtils.isCurrentUserInRole("HR") && !SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
            String hrLocation = SecurityUtils.getCurrentUserLocation();
            SurveyResponseDto existing = surveyService.getSurveyById(id);
            if (!existing.getLocation().equalsIgnoreCase(hrLocation)) {
                throw new BadRequestException("You are not authorized to edit questions for this survey.");
            }
        }

        SurveyResponseDto response = surveyService.addQuestionsToSurvey(id, questionIds);
        return ResponseEntity.ok(ApiResponse.success(response, "Questions selected and linked successfully"));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'HR', 'GLOBAL_HR')")
    @Operation(summary = "Get the active survey for current user's location")
    public ResponseEntity<ApiResponse<SurveyResponseDto>> getActiveSurvey() {
        String location = SecurityUtils.getCurrentUserLocation();
        if (location == null) {
            throw new BadRequestException("No location registered for user profile. Cannot query active survey.");
        }
        
        String employeeId = SecurityUtils.getCurrentUsername();
        SurveyResponseDto survey = surveyService.getActiveSurveyForLocation(location, employeeId);
        if (survey == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No active survey found for your location: " + location));
        }
        
        return ResponseEntity.ok(ApiResponse.success(survey, "Active survey retrieved successfully"));
    }

    @PostMapping("/rollout")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Rollout approved AI questions to the active survey")
    public ResponseEntity<ApiResponse<Void>> rolloutSurvey() {
        String location = SecurityUtils.getCurrentUserLocation();
        surveyService.rolloutSurvey(location);
        return ResponseEntity.ok(ApiResponse.success("Survey rolled out successfully. Employees have been notified."));
    }
}

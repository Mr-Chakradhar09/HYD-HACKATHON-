package com.pulse.survey.controller;

import com.pulse.survey.dto.ApiResponse;
import com.pulse.survey.dto.request.DraftQuestionRequest;
import com.pulse.survey.dto.response.DraftQuestionResponse;
import com.pulse.survey.dto.response.QuestionResponse;
import com.pulse.survey.enums.DraftQuestionStatus;
import com.pulse.survey.service.DraftQuestionService;
import com.pulse.survey.service.HRApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/draft-questions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Draft Question Controller", description = "Endpoints for reviewing and approving AI-suggested questions")
public class DraftQuestionController {

    private final DraftQuestionService draftQuestionService;
    private final HRApprovalService hrApprovalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Get list of draft questions (filter by PENDING, APPROVED, REJECTED)")
    public ResponseEntity<ApiResponse<Page<DraftQuestionResponse>>> getDraftQuestions(
            @RequestParam(value = "status", required = false) DraftQuestionStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "id,desc") String sort) {

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<DraftQuestionResponse> drafts = draftQuestionService.getDraftQuestions(pageable, status);
        return ResponseEntity.ok(ApiResponse.success(drafts, "Fetched draft questions successfully"));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Approve an AI-suggested question and copy it to the Question Bank")
    public ResponseEntity<ApiResponse<QuestionResponse>> approveDraftQuestion(@PathVariable Long id) {
        QuestionResponse approved = hrApprovalService.approveDraftQuestion(id);
        return ResponseEntity.ok(ApiResponse.success(approved, "AI draft question approved. Added to Global Question Bank."));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Reject an AI-suggested question")
    public ResponseEntity<ApiResponse<Void>> rejectDraftQuestion(@PathVariable Long id) {
        hrApprovalService.rejectDraftQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("AI draft question rejected successfully"));
    }

    @PutMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('HR', 'GLOBAL_HR')")
    @Operation(summary = "Edit an AI-suggested question draft")
    public ResponseEntity<ApiResponse<DraftQuestionResponse>> editDraftQuestion(
            @PathVariable Long id,
            @Valid @RequestBody DraftQuestionRequest request) {
        
        DraftQuestionResponse updated = draftQuestionService.editDraftQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "AI draft question edited successfully"));
    }
}

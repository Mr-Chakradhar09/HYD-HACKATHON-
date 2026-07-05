package com.virtusa.pulse.ai.controller;

import com.virtusa.pulse.ai.dto.*;
import com.virtusa.pulse.ai.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Sentiment & Question Curation API", description = "Endpoints for analyzing survey comment sentiments and generating follow-up questions from database logs.")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/reports/generate")
    @Operation(summary = "Analyze survey feedbacks & generate sentiment report",
            description = "Receives raw ratings and comments for a location, parses sentiments using Gemini, compiles a report, and persists it in MySQL database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sentiment report generated successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SentimentReportResponseDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid feedback request payload",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "LLM analysis or DB persistence failure",
                    content = @Content)
    })
    public ResponseEntity<SentimentReportResponseDto> generateReport(
            @Valid @RequestBody FeedbackRequest request) {
        log.info("Received request to generate sentiment report for survey ID: {}, location: {}",
                request.getSurveyId(), request.getLocation());

        SentimentReportResponseDto response = aiService.generateReport(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/questions/generate")
    @Operation(summary = "Draft next month's follow-up questions",
            description = "Retrieves the previous month's sentiment report and employee feedback from MySQL, invokes Gemini to draft 10 follow-up questions, saves drafts to the Survey Service, and alerts HR via Kafka.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Draft questions curated successfully",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = QuestionDraftDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid question generation metadata",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Draft compilation failure",
                    content = @Content)
    })
    public ResponseEntity<List<QuestionDraftDto>> generateQuestions(
            @Valid @RequestBody QuestionGenerationRequest request) {
        log.info("Received request to generate follow-up questions from source survey ID: {} to target survey ID: {} in {}",
                request.getSourceSurveyId(), request.getTargetSurveyId(), request.getLocation());

        List<QuestionDraftDto> drafts = aiService.generateQuestions(
                request.getSourceSurveyId(),
                request.getLocation(),
                request.getTargetSurveyId()
        );
        return ResponseEntity.ok(drafts);
    }

    @PostMapping("/generate-questions")
    @Operation(summary = "Draft targeted employee follow-up questions",
            description = "Analyzes employee responses directly to generate targeted follow up questions for the specific employee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Draft questions generated successfully")
    })
    public ResponseEntity<List<QuestionDraftDto>> generateQuestionsFromAnswers(
            @Valid @RequestBody AIAnalysisRequest request) {
        log.info("Received request to generate targeted questions for survey ID: {}", request.getSurveyId());
        List<QuestionDraftDto> drafts = aiService.generateQuestionsFromAnswers(request);
        return ResponseEntity.ok(drafts);
    }
}

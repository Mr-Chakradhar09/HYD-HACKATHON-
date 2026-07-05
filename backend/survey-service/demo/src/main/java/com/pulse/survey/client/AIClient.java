package com.pulse.survey.client;

import com.pulse.survey.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ai-service", url = "${AI_SERVICE_URL:http://localhost:8083}")
public interface AIClient {

    @PostMapping("/api/ai/generate-questions")
    ResponseEntity<List<SuggestedQuestionDto>> generateQuestions(@RequestBody AIAnalysisRequest request);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class AIAnalysisRequest {
        private Long surveyId;
        private String title;
        private String location;
        private List<AnswerDetail> answers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class AnswerDetail {
        private String employeeId;
        private String questionText;
        private String category;
        private Integer rating;
        private String comment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class SuggestedQuestionDto {
        private String targetEmployeeId;
        private String questionText;
        private String category;
        private QuestionType questionType;
        private String reason;
        private Double confidence;
    }
}

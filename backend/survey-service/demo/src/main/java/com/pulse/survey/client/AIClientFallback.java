package com.pulse.survey.client;

import com.pulse.survey.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AIClientFallback implements AIClient {

    @Override
    public ResponseEntity<List<SuggestedQuestionDto>> generateQuestions(AIAnalysisRequest request) {
        log.error("AIClient fallback triggered for generateQuestions. AI Service is down.");
        throw new BadRequestException("AI Analysis Service is currently down or unresponsive. Suggested questions cannot be generated at this time.");
    }
}

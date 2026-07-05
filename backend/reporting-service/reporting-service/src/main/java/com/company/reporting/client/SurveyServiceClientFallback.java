package com.company.reporting.client;

import org.springframework.stereotype.Component;
import java.util.Collections;

@Component
public class SurveyServiceClientFallback implements SurveyServiceClient {

    @Override
    public SurveyResponse getSurveyById(Long id) {
        SurveyResponse response = new SurveyResponse();
        response.setId(id);
        response.setTitle("Fallback Survey");
        response.setMonth("2026-07");
        response.setIsPublished(false);
        response.setQuestions(Collections.emptyList());
        return response;
    }
}

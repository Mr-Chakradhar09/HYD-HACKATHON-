package com.company.reporting.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "survey-service", url = "${app.services.survey-service-url:http://localhost:8082}", fallback = com.company.reporting.client.SurveyServiceClientFallback.class)
public interface SurveyServiceClient {

    @GetMapping("/api/surveys/{id}")
    SurveyResponse getSurveyById(@PathVariable("id") Long id);

    @Data
    class SurveyResponse {
        private Long id;
        private String title;
        private String month;
        private Boolean isPublished;
        private List<String> questions;
    }
}

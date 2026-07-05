package com.company.reporting.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sentiment-analysis-service", url = "${app.services.sentiment-analysis-service-url:http://localhost:8084}", fallback = com.company.reporting.client.SentimentServiceClientFallback.class)
public interface SentimentServiceClient {

    @PostMapping("/api/sentiment/analyze")
    SentimentResponse analyzeSentiment(@RequestBody SentimentRequest request);

    @Data
    class SentimentRequest {
        private String text;
    }

    @Data
    class SentimentResponse {
        private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE
        private Double score; // 0.0 to 100.0
        private String burnoutRisk; // LOW, MEDIUM, HIGH
    }
}

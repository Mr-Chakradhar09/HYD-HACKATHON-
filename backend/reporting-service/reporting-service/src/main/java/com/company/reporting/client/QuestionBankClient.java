package com.company.reporting.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "question-bank-service", url = "${app.services.question-bank-service-url:http://localhost:8083}", fallback = com.company.reporting.client.QuestionBankClientFallback.class)
public interface QuestionBankClient {

    @GetMapping("/api/questions/{id}")
    QuestionResponse getQuestionById(@PathVariable("id") Long id);

    @GetMapping("/api/questions/theme/{theme}")
    List<QuestionResponse> getQuestionsByTheme(@PathVariable("theme") String theme);

    @Data
    class QuestionResponse {
        private Long id;
        private String text;
        private String theme; // Work-Life Balance, Career Growth, etc.
        private String type; // RATING, MULTIPLE_CHOICE, OPEN_TEXT
    }
}

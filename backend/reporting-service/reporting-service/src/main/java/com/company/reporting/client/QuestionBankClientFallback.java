package com.company.reporting.client;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class QuestionBankClientFallback implements QuestionBankClient {

    @Override
    public QuestionResponse getQuestionById(Long id) {
        QuestionResponse response = new QuestionResponse();
        response.setId(id);
        response.setText("Fallback Question");
        response.setTheme("Work-Life Balance");
        response.setType("RATING");
        return response;
    }

    @Override
    public List<QuestionResponse> getQuestionsByTheme(String theme) {
        return Collections.emptyList();
    }
}

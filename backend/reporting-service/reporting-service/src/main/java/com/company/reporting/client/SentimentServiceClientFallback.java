package com.company.reporting.client;

import org.springframework.stereotype.Component;

@Component
public class SentimentServiceClientFallback implements SentimentServiceClient {

    @Override
    public SentimentResponse analyzeSentiment(SentimentRequest request) {
        SentimentResponse response = new SentimentResponse();
        response.setSentiment("NEUTRAL");
        response.setScore(50.0);
        response.setBurnoutRisk("LOW");
        return response;
    }
}

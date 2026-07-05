package com.company.reporting.service;

import com.company.reporting.dto.AIInsightDTO;
import java.util.List;

public interface InsightService {
    AIInsightDTO getInsightByMonth(String month);
    List<String> getTopConcerns(String month);
    String getSentimentSummary(String month);
    String getLocationAnalysis(String month);
    void generateAIInsights(String month);
}

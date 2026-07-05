package com.company.reporting.service;

import com.company.reporting.entity.SurveySnapshot;

public interface AnalyticsService {
    void processSurveySubmission(SurveySnapshot snapshot);
    void aggregateMonthlyData(String month);
}

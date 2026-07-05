package com.virtusa.pulse.ai.service;

import com.virtusa.pulse.ai.dto.*;
import java.util.List;

public interface AIService {
    SentimentReportResponseDto generateReport(FeedbackRequest request);
    List<QuestionDraftDto> generateQuestions(Long sourceSurveyId, String location, Long targetSurveyId);
    List<QuestionDraftDto> generateQuestionsFromAnswers(AIAnalysisRequest request);
}

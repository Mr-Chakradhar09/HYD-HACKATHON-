package com.virtusa.pulse.ai.service;

import com.virtusa.pulse.ai.client.SurveyServiceClient;
import com.virtusa.pulse.ai.dto.*;
import com.virtusa.pulse.ai.entity.SentimentDetail;
import com.virtusa.pulse.ai.entity.SentimentReport;
import com.virtusa.pulse.ai.producer.NotificationProducer;
import com.virtusa.pulse.ai.repository.SentimentReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service("mockAIService")
public class MockAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(MockAIService.class);

    private final SentimentReportRepository reportRepository;
    private final SurveyServiceClient surveyServiceClient;
    private final NotificationProducer notificationProducer;

    public MockAIService(SentimentReportRepository reportRepository,
                          SurveyServiceClient surveyServiceClient,
                          NotificationProducer notificationProducer) {
        this.reportRepository = reportRepository;
        this.surveyServiceClient = surveyServiceClient;
        this.notificationProducer = notificationProducer;
    }

    @Override
    @Transactional
    public SentimentReportResponseDto generateReport(FeedbackRequest request) {
        log.info("[MOCK] Generating sentiment report for survey {} in {}", request.getSurveyId(), request.getLocation());

        double totalRating = 0;
        int count = 0;
        List<SentimentDetail> details = new ArrayList<>();
        List<SentimentDetailDto> detailDtos = new ArrayList<>();

        for (FeedbackItem item : request.getFeedbacks()) {
            totalRating += item.getRating();
            count++;

            String label = "Neutral";
            double score = 50.0;
            String theme = "General Feedback";

            String comment = item.getComment();
            if (comment != null && !comment.isBlank()) {
                String lowerComment = comment.toLowerCase();
                if (lowerComment.contains("good") || lowerComment.contains("great") || lowerComment.contains("excellent") || lowerComment.contains("satisfied")) {
                    label = "Positive";
                    score = 85.0;
                    theme = "High Satisfaction";
                } else if (lowerComment.contains("poor") || lowerComment.contains("bad") || lowerComment.contains("fail") || lowerComment.contains("slow") || lowerComment.contains("improve")) {
                    label = "Negative";
                    score = 20.0;
                    theme = "Area for Improvement";
                }
            }

            SentimentDetail detail = new SentimentDetail(
                    item.getFeedbackId(),
                    item.getRating(),
                    comment,
                    score,
                    label,
                    theme
            );
            details.add(detail);

            detailDtos.add(new SentimentDetailDto(
                    item.getFeedbackId(),
                    item.getRating(),
                    comment,
                    score,
                    label,
                    theme
            ));
        }

        double averageScore = count > 0 ? (totalRating / count) : 0.0;
        String overallSentiment = "Neutral";
        if (averageScore >= 4.0) {
            overallSentiment = "Positive";
        } else if (averageScore < 3.0) {
            overallSentiment = "Negative";
        }

        String summary = String.format("Analyzed %d responses for %s office. Average rating is %.2f. Sentiment is predominantly %s.",
                count, request.getLocation(), averageScore, overallSentiment);

        // Save report to MySQL repository
        SentimentReport report = new SentimentReport(
                request.getSurveyId(),
                request.getLocation(),
                averageScore,
                overallSentiment,
                summary,
                LocalDateTime.now()
        );
        for (SentimentDetail d : details) {
            report.addDetail(d);
        }
        SentimentReport savedReport = reportRepository.save(report);

        return new SentimentReportResponseDto(
                savedReport.getId(),
                request.getSurveyId(),
                request.getLocation(),
                averageScore,
                overallSentiment,
                summary,
                detailDtos
        );
    }

    @Override
    @Transactional
    public List<QuestionDraftDto> generateQuestions(Long sourceSurveyId, String location, Long targetSurveyId) {
        log.info("[MOCK] Generating questions based on survey {} for target survey {} in {}", sourceSurveyId, targetSurveyId, location);

        // Try to fetch report from DB
        List<SentimentReport> reports = reportRepository.findBySurveyId(sourceSurveyId);
        SentimentReport sourceReport = reports.stream()
                .filter(r -> location.equalsIgnoreCase(r.getLocation()))
                .findFirst()
                .orElse(null);

        String summaryContext;
        double averageScore = 3.5;
        if (sourceReport != null) {
            summaryContext = sourceReport.getSentimentSummary();
            averageScore = sourceReport.getAverageScore();
            log.info("[MOCK] Found existing report ID {} for source survey", sourceReport.getId());
        } else {
            summaryContext = "No existing report found. Generating general questions.";
            log.warn("[MOCK] No sentiment report found in DB for survey {} in {}. Using default mock details.", sourceSurveyId, location);
        }

        // Generate 10 mock questions
        List<QuestionDraftDto> drafts = new ArrayList<>();
        String[] categories = {"Workplace Environment", "Management & Communication", "Tools & Technology", "Work-Life Balance"};
        for (int i = 1; i <= 10; i++) {
            String category = categories[i % categories.length];
            drafts.add(new QuestionDraftDto(
                    String.format("[Mock Question %d] How would you rate the %s aspect of the %s location, given: %s?", i, category.toLowerCase(), location, summaryContext.substring(0, Math.min(30, summaryContext.length()))),
                    category,
                    location,
                    targetSurveyId
            ));
        }

        // Call Survey Service to save drafts (with resilient try-catch)
        try {
            log.info("[MOCK] Saving 10 drafted questions to Survey Service...");
            surveyServiceClient.saveDraftQuestions(drafts);
        } catch (Exception e) {
            log.warn("[MOCK] Failed to connect with Survey Service. Proceeding offline. Error: {}", e.getMessage());
        }

        // Call Notification Service (Kafka)
        try {
            log.info("[MOCK] Publishing notification to HR via Kafka...");
            NotificationRequestDto notification = new NotificationRequestDto(
                    "HR",
                    location,
                    "EMAIL",
                    "New AI Question Drafts Available",
                    String.format("AI has compiled 10 new draft questions for survey %d based on previous survey %d feedbacks (Avg rating: %.2f) in %s.",
                            targetSurveyId, sourceSurveyId, averageScore, location)
            );
            notificationProducer.sendNotification(notification);
        } catch (Exception e) {
            log.warn("[MOCK] Failed to publish Kafka notification. Proceeding offline. Error: {}", e.getMessage());
        }

        return drafts;
    }

    @Override
    public List<QuestionDraftDto> generateQuestionsFromAnswers(AIAnalysisRequest request) {
        List<QuestionDraftDto> drafts = new ArrayList<>();
        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            for (int i = 0; i < Math.min(5, request.getAnswers().size()); i++) {
                AIAnalysisRequest.AnswerDetail ans = request.getAnswers().get(i);
                QuestionDraftDto draft = new QuestionDraftDto();
                draft.setText("[Global Follow-Up Mock] How can we improve based on general feedback regarding: " + ans.getComment() + "?");
                draft.setCategory(ans.getCategory() != null ? ans.getCategory() : "Follow Up");
                draft.setLocation(request.getLocation());
                draft.setSurveyId(request.getSurveyId());
                drafts.add(draft);
            }
        }
        return drafts;
    }
}

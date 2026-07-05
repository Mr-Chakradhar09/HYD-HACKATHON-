package com.virtusa.pulse.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtusa.pulse.ai.client.SurveyServiceClient;
import com.virtusa.pulse.ai.dto.*;
import com.virtusa.pulse.ai.entity.SentimentDetail;
import com.virtusa.pulse.ai.entity.SentimentReport;
import com.virtusa.pulse.ai.producer.NotificationProducer;
import com.virtusa.pulse.ai.repository.SentimentReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("geminiAIService")
@Primary
public class GeminiAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);

    private final ChatModel chatModel;
    private final SentimentReportRepository reportRepository;
    private final SurveyServiceClient surveyServiceClient;
    private final NotificationProducer notificationProducer;
    private final MockAIService mockAIService;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.google.genai.api-key:mock-key}")
    private String apiKey;

    public GeminiAIService(ChatModel chatModel,
                             SentimentReportRepository reportRepository,
                             SurveyServiceClient surveyServiceClient,
                             NotificationProducer notificationProducer,
                             MockAIService mockAIService) {
        this.chatModel = chatModel;
        this.reportRepository = reportRepository;
        this.surveyServiceClient = surveyServiceClient;
        this.notificationProducer = notificationProducer;
        this.mockAIService = mockAIService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Transactional
    public SentimentReportResponseDto generateReport(FeedbackRequest request) {
        if ("mock-key".equalsIgnoreCase(apiKey) || apiKey.isBlank()) {
            log.info("Gemini API key is not configured. Falling back to MockAIService.generateReport.");
            return mockAIService.generateReport(request);
        }

        try {
            log.info("Initiating Gemini AI sentiment analysis to generate report for survey {} in {}", request.getSurveyId(), request.getLocation());

            String systemPrompt = "You are an expert HR Sentiment Analyst.\n" +
                    "Analyze the following employee comments and ratings for survey ID " + request.getSurveyId() + " in location " + request.getLocation() + ".\n" +
                    "Generate:\n" +
                    "1. An individual sentiment analysis for each feedback item containing:\n" +
                    "   - feedbackId\n" +
                    "   - sentimentScore (a value between 0.0 and 100.0)\n" +
                    "   - sentimentLabel (Positive, Neutral, or Negative)\n" +
                    "   - detectedTheme (short theme summary, max 5 words)\n" +
                    "2. An overall average sentiment label and a summary (max 3 sentences) of key themes and findings.\n\n" +
                    "You must return the response in strict JSON format matching this schema:\n" +
                    "{\n" +
                    "  \"overallSentiment\": \"Positive/Neutral/Negative\",\n" +
                    "  \"sentimentSummary\": \"Summary here...\",\n" +
                    "  \"feedbacks\": [\n" +
                    "    {\n" +
                    "      \"feedbackId\": 1001,\n" +
                    "      \"sentimentScore\": 85.0,\n" +
                    "      \"sentimentLabel\": \"Positive\",\n" +
                    "      \"detectedTheme\": \"Theme description\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n\n" +
                    "Do not include any text outside this JSON block. Do not wrap the JSON block in markdown backticks like ```json.";

            StringBuilder userPrompt = new StringBuilder("Here is the list of employee responses:\n");
            for (FeedbackItem item : request.getFeedbacks()) {
                userPrompt.append(String.format("- Feedback ID: %d, Rating: %d, Comment: %s\n",
                        item.getFeedbackId(), item.getRating(), item.getComment()));
            }

            String promptText = systemPrompt + "\n" + userPrompt.toString();
            log.info("Sending report prompt to Gemini...");
            ChatResponse response = chatModel.call(new Prompt(promptText));
            String rawOutput = response.getResult().getOutput().getText();

            String cleanedOutput = rawOutput.trim();
            if (cleanedOutput.startsWith("```")) {
                cleanedOutput = cleanedOutput.replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "").trim();
            }

            JsonNode rootNode = objectMapper.readTree(cleanedOutput);

            String overallSentiment = rootNode.path("overallSentiment").asText("Neutral");
            String sentimentSummary = rootNode.path("sentimentSummary").asText("Analysis completed.");

            // Parse individual feedback details
            JsonNode feedbacksNode = rootNode.path("feedbacks");
            Map<Long, SentimentDetailDto> sentimentDetailsMap = new HashMap<>();
            if (feedbacksNode.isArray()) {
                for (JsonNode fNode : feedbacksNode) {
                    Long fbId = fNode.path("feedbackId").asLong();
                    double sScore = fNode.path("sentimentScore").asDouble(50.0);
                    String sLabel = fNode.path("sentimentLabel").asText("Neutral");
                    String sTheme = fNode.path("detectedTheme").asText("General Feedback");
                    sentimentDetailsMap.put(fbId, new SentimentDetailDto(fbId, 0, "", sScore, sLabel, sTheme));
                }
            }

            double totalRating = 0.0;
            int count = 0;
            List<SentimentDetail> details = new ArrayList<>();
            List<SentimentDetailDto> responseDetails = new ArrayList<>();

            for (FeedbackItem item : request.getFeedbacks()) {
                totalRating += item.getRating();
                count++;

                SentimentDetailDto aiDetail = sentimentDetailsMap.getOrDefault(item.getFeedbackId(),
                        new SentimentDetailDto(item.getFeedbackId(), item.getRating(), item.getComment(), 50.0, "Neutral", "General Feedback"));

                SentimentDetail detail = new SentimentDetail(
                        item.getFeedbackId(),
                        item.getRating(),
                        item.getComment(),
                        aiDetail.getSentimentScore(),
                        aiDetail.getSentimentLabel(),
                        aiDetail.getDetectedTheme()
                );
                details.add(detail);

                responseDetails.add(new SentimentDetailDto(
                        item.getFeedbackId(),
                        item.getRating(),
                        item.getComment(),
                        aiDetail.getSentimentScore(),
                        aiDetail.getSentimentLabel(),
                        aiDetail.getDetectedTheme()
                ));
            }

            double averageScore = count > 0 ? (totalRating / count) : 0.0;

            // Save report to MySQL
            SentimentReport report = new SentimentReport(
                    request.getSurveyId(),
                    request.getLocation(),
                    averageScore,
                    overallSentiment,
                    sentimentSummary,
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
                    sentimentSummary,
                    responseDetails
            );

        } catch (Exception e) {
            log.error("Error generating report using Gemini. Falling back to MockAIService. Error: {}", e.getMessage(), e);
            return mockAIService.generateReport(request);
        }
    }

    @Override
    @Transactional
    public List<QuestionDraftDto> generateQuestions(Long sourceSurveyId, String location, Long targetSurveyId) {
        if ("mock-key".equalsIgnoreCase(apiKey) || apiKey.isBlank()) {
            log.info("Gemini API key is not configured. Falling back to MockAIService.generateQuestions.");
            return mockAIService.generateQuestions(sourceSurveyId, location, targetSurveyId);
        }

        try {
            log.info("Fetching previous month's sentiment report for survey {} in {}", sourceSurveyId, location);

            List<SentimentReport> reports = reportRepository.findBySurveyId(sourceSurveyId);
            SentimentReport sourceReport = reports.stream()
                    .filter(r -> location.equalsIgnoreCase(r.getLocation()))
                    .findFirst()
                    .orElse(null);

            if (sourceReport == null) {
                log.warn("No sentiment report found in DB for survey {} in {}. Falling back to Mock logic.", sourceSurveyId, location);
                return mockAIService.generateQuestions(sourceSurveyId, location, targetSurveyId);
            }

            log.info("Found report ID {}. Extracted summary: {}", sourceReport.getId(), sourceReport.getSentimentSummary());

            // Build comments list context
            StringBuilder commentsList = new StringBuilder();
            for (SentimentDetail detail : sourceReport.getDetails()) {
                if (detail.getComment() != null && !detail.getComment().isBlank()) {
                    commentsList.append(String.format("- Comment (Rating %d): %s [Sentiment: %s, Theme: %s]\n",
                            detail.getRating(), detail.getComment(), detail.getSentimentLabel(), detail.getDetectedTheme()));
                }
            }

            String systemPrompt = "You are an expert HR Specialist and Survey Curation Specialist.\n" +
                    "We are generating 10 new monthly pulse survey questions for the next monthly survey (ID " + targetSurveyId + ") at location " + location + ".\n" +
                    "These questions must investigate and follow up on recurring issues, complaints, and general themes from the previous month's survey.\n\n" +
                    "Here is the sentiment analysis summary of the previous month's survey:\n" +
                    "\"" + sourceReport.getSentimentSummary() + "\"\n\n" +
                    "Here is the list of employee comments collected last month:\n" +
                    commentsList.toString() + "\n\n" +
                    "Generate exactly 10 new targeted pulse survey questions in strict JSON format. Each question must include:\n" +
                    " - text (question string)\n" +
                    " - category (e.g. Workplace, Management, Tools, Balance)\n" +
                    " - location (must be '" + location + "')\n" +
                    " - surveyId (must be " + targetSurveyId + ")\n\n" +
                    "You must return the response in strict JSON format matching this schema:\n" +
                    "{\n" +
                    "  \"draftedQuestions\": [\n" +
                    "    {\n" +
                    "      \"text\": \"Question text?\",\n" +
                    "      \"category\": \"Category name\",\n" +
                    "      \"location\": \"" + location + "\",\n" +
                    "      \"surveyId\": " + targetSurveyId + "\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n\n" +
                    "Do not include any text outside this JSON block. Do not wrap the JSON block in markdown backticks like ```json.";

            log.info("Sending question drafting prompt to Gemini...");
            ChatResponse response = chatModel.call(new Prompt(systemPrompt));
            String rawOutput = response.getResult().getOutput().getText();

            String cleanedOutput = rawOutput.trim();
            if (cleanedOutput.startsWith("```")) {
                cleanedOutput = cleanedOutput.replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "").trim();
            }

            JsonNode rootNode = objectMapper.readTree(cleanedOutput);
            JsonNode questionsNode = rootNode.path("draftedQuestions");
            List<QuestionDraftDto> drafts = new ArrayList<>();

            if (questionsNode.isArray()) {
                for (JsonNode qNode : questionsNode) {
                    String qText = qNode.path("text").asText();
                    String qCat = qNode.path("category").asText("General");
                    if (qText != null && !qText.isBlank()) {
                        drafts.add(new QuestionDraftDto(
                                qText,
                                qCat,
                                location,
                                targetSurveyId
                        ));
                    }
                }
            }

            // Fallback questions if AI failed to return exactly 10
            if (drafts.isEmpty()) {
                log.warn("Gemini returned empty drafts. Using fallback curation.");
                return mockAIService.generateQuestions(sourceSurveyId, location, targetSurveyId);
            }

            // Downstream Feign call to Survey Service
            try {
                log.info("Saving drafted questions to Survey Service via Feign Client...");
                surveyServiceClient.saveDraftQuestions(drafts);
            } catch (Exception e) {
                log.warn("Downstream Survey Service is unavailable. Drafts will remain saved in analytics report only. Error: {}", e.getMessage());
            }

            // Downstream Kafka notification to HR
            try {
                log.info("Publishing alert notification to HR via Kafka...");
                NotificationRequestDto notification = new NotificationRequestDto(
                        "HR",
                        location,
                        "EMAIL",
                        "AI Sentiment Analysis & Question Drafts Ready",
                        String.format("Gemini has compiled 10 new draft questions for survey %d based on previous survey %d feedbacks (Avg rating: %.2f) in %s.",
                                targetSurveyId, sourceSurveyId, sourceReport.getAverageScore(), location)
                );
                notificationProducer.sendNotification(notification);
            } catch (Exception e) {
                log.warn("Kafka broker is unavailable. Notification event skipped. Error: {}", e.getMessage());
            }

            return drafts;

        } catch (Exception e) {
            log.error("Error generating questions using Gemini. Falling back to MockAIService. Error: {}", e.getMessage(), e);
            return mockAIService.generateQuestions(sourceSurveyId, location, targetSurveyId);
        }
    }

    @Override
    @Transactional
    public List<QuestionDraftDto> generateQuestionsFromAnswers(AIAnalysisRequest request) {
        if ("mock-key".equalsIgnoreCase(apiKey) || apiKey.isBlank()) {
            return mockAIService.generateQuestionsFromAnswers(request);
        }

        try {
            log.info("Generating global follow-up questions from answers using Gemini for survey {}", request.getSurveyId());
            List<QuestionDraftDto> drafts = new ArrayList<>();

            StringBuilder commentsContext = new StringBuilder();
            for (AIAnalysisRequest.AnswerDetail ans : request.getAnswers()) {
                if (ans.getComment() != null && !ans.getComment().isBlank()) {
                    commentsContext.append("- [").append(ans.getCategory()).append("] ").append(ans.getComment()).append("\n");
                }
            }

            if (commentsContext.length() == 0) {
                log.info("No comments found in answers. Cannot generate follow up questions.");
                return drafts;
            }

            String prompt = "You are an expert HR Specialist. Here are recent employee comments from a pulse survey at location " + request.getLocation() + ":\n" +
                    commentsContext.toString() + "\n" +
                    "Generate exactly 5 insightful follow-up questions to ask all employees in the next global survey to gather more details or address these concerns.\n" +
                    "You must return the response in strict JSON format matching this schema:\n" +
                    "{\n" +
                    "  \"draftedQuestions\": [\n" +
                    "    {\n" +
                    "      \"text\": \"Question text?\",\n" +
                    "      \"category\": \"Category name\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n" +
                    "Do not include any text outside this JSON block.";

            ChatResponse response = chatModel.call(new Prompt(prompt));
            String rawOutput = response.getResult().getOutput().getText().trim();
            
            if (rawOutput.startsWith("```")) {
                rawOutput = rawOutput.replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "").trim();
            }

            JsonNode rootNode = objectMapper.readTree(rawOutput);
            JsonNode questionsNode = rootNode.path("draftedQuestions");

            if (questionsNode.isArray()) {
                for (JsonNode qNode : questionsNode) {
                    String qText = qNode.path("text").asText();
                    String qCat = qNode.path("category").asText("Follow Up");
                    if (qText != null && !qText.isBlank()) {
                        QuestionDraftDto draft = new QuestionDraftDto();
                        draft.setText(qText);
                        draft.setCategory(qCat);
                        draft.setLocation(request.getLocation());
                        draft.setSurveyId(request.getSurveyId());
                        drafts.add(draft);
                    }
                }
            }

            if (!drafts.isEmpty()) {
                try {
                    surveyServiceClient.saveDraftQuestions(drafts);
                } catch (Exception e) {
                    log.warn("Downstream Survey Service is unavailable. Error: {}", e.getMessage());
                }
            }
            return drafts;
        } catch (Exception e) {
            log.error("Error in generateQuestionsFromAnswers using Gemini. Falling back to MockAIService. Error: {}", e.getMessage(), e);
            return mockAIService.generateQuestionsFromAnswers(request);
        }
    }
}

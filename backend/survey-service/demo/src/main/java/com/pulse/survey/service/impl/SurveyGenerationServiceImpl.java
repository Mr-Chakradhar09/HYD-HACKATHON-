package com.pulse.survey.service.impl;

import com.pulse.survey.client.AIClient;
import com.pulse.survey.entity.DraftQuestion;
import com.pulse.survey.entity.Survey;
import com.pulse.survey.entity.SurveyAnswer;
import com.pulse.survey.enums.DraftQuestionStatus;
import com.pulse.survey.enums.SurveyStatus;
import com.pulse.survey.exception.BadRequestException;
import com.pulse.survey.exception.ResourceNotFoundException;
import com.pulse.survey.repository.DraftQuestionRepository;
import com.pulse.survey.repository.SurveyAnswerRepository;
import com.pulse.survey.repository.SurveyRepository;
import com.pulse.survey.service.NotificationProducer;
import com.pulse.survey.service.SurveyGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyGenerationServiceImpl implements SurveyGenerationService {

    private final SurveyRepository surveyRepository;
    private final SurveyAnswerRepository answerRepository;
    private final DraftQuestionRepository draftQuestionRepository;
    private final AIClient aiClient;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public void generateAIQuestions(Long surveyId) {
        log.info("Starting AI Question Generation process for survey ID: {}", surveyId);
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + surveyId));

        if (survey.getStatus() != SurveyStatus.CLOSED) {
            throw new BadRequestException("AI questions can only be generated for CLOSED surveys. Current status: " + survey.getStatus());
        }

        List<SurveyAnswer> answers = answerRepository.findByResponseSurveyId(surveyId);
        if (answers.isEmpty()) {
            throw new BadRequestException("No employee responses/answers found for survey ID: " + surveyId + ". Cannot generate AI questions.");
        }

        // Map answers to AIClient.AnswerDetail objects
        List<AIClient.AnswerDetail> answerDetails = answers.stream()
                .map(ans -> AIClient.AnswerDetail.builder()
                        .employeeId(ans.getResponse().getEmployeeId())
                        .questionText(ans.getQuestion().getQuestionText())
                        .category(ans.getQuestion().getCategory())
                        .rating(ans.getRating())
                        .comment(ans.getComment())
                        .build())
                .collect(Collectors.toList());

        AIClient.AIAnalysisRequest aiRequest = AIClient.AIAnalysisRequest.builder()
                .surveyId(surveyId)
                .title(survey.getTitle())
                .location(survey.getLocation())
                .answers(answerDetails)
                .build();

        log.info("Sending analysis payload to AI service via Feign client...");
        ResponseEntity<List<AIClient.SuggestedQuestionDto>> responseEntity = aiClient.generateQuestions(aiRequest);
        List<AIClient.SuggestedQuestionDto> suggestions = responseEntity.getBody();

        if (suggestions == null || suggestions.isEmpty()) {
            log.warn("AI Service returned empty or null suggestions list.");
            return;
        }

        log.info("AI Service returned {} question suggestions. Storing as PENDING DraftQuestions...", suggestions.size());

        List<DraftQuestion> drafts = suggestions.stream()
                .map(s -> DraftQuestion.builder()
                        .questionText(s.getQuestionText())
                        .category(s.getCategory())
                        .questionType(s.getQuestionType())
                        .reason(s.getReason())
                        .confidence(s.getConfidence())
                        .targetEmployeeId(s.getTargetEmployeeId())
                        .status(DraftQuestionStatus.PENDING)
                        .generatedDate(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        draftQuestionRepository.saveAll(drafts);
        log.info("Successfully persisted {} draft questions in MySQL.", drafts.size());

        // Notify GLOBAL_HR and HR that draft questions are ready for review
        notificationProducer.sendDraftQuestionsGeneratedNotification(surveyId, drafts.size());
    }
}

package com.pulse.survey.service.impl;

import com.pulse.survey.dto.request.SurveySubmissionRequest;
import com.pulse.survey.dto.response.SubmissionStatusResponse;
import com.pulse.survey.dto.response.SurveyReportResponse;
import com.pulse.survey.entity.*;
import com.pulse.survey.enums.QuestionType;
import com.pulse.survey.enums.SurveyStatus;
import com.pulse.survey.exception.BadRequestException;
import com.pulse.survey.exception.ResourceNotFoundException;
import com.pulse.survey.repository.*;
import com.pulse.survey.security.SecurityUtils;
import com.pulse.survey.service.ResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResponseServiceImpl implements ResponseService {

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyAnswerRepository answerRepository;

    @Override
    @Transactional
    public SubmissionStatusResponse submitSurveyResponse(SurveySubmissionRequest request, String employeeId) {
        log.info("Submitting response for survey ID: {} by employee: {}", request.getSurveyId(), employeeId);

        Survey survey = surveyRepository.findById(request.getSurveyId())
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + request.getSurveyId()));

        if (survey.getStatus() != SurveyStatus.PUBLISHED) {
            throw new BadRequestException("Responses can only be submitted to PUBLISHED surveys. Current status: " + survey.getStatus());
        }

        // Validate employee location matches survey location
        String empLocation = SecurityUtils.getCurrentUserLocation();
        if (empLocation != null && !empLocation.equalsIgnoreCase(survey.getLocation())) {
            // Check if user is GLOBAL_HR (global HR can test or submit, but employees are strictly locked to location)
            if (!SecurityUtils.isCurrentUserInRole("GLOBAL_HR")) {
                throw new BadRequestException("Survey is not targeted for your location. Your location: " 
                        + empLocation + ", Survey location: " + survey.getLocation());
            }
        }

        // Prevent duplicate submissions
        if (responseRepository.existsBySurveyIdAndEmployeeId(survey.getId(), employeeId)) {
            throw new BadRequestException("You have already submitted a response for this survey.");
        }

        // Fetch valid questions associated with this survey to prevent illegal answers
        List<Long> allowedQuestionIds = surveyQuestionRepository.findBySurveyIdOrderByDisplayOrderAsc(survey.getId())
                .stream()
                .map(sq -> sq.getQuestion().getId())
                .collect(Collectors.toList());

        // Construct SurveyResponse
        SurveyResponse response = SurveyResponse.builder()
                .survey(survey)
                .employeeId(employeeId)
                .submittedAt(LocalDateTime.now())
                .build();

        SurveyResponse savedResponse = responseRepository.save(response);
        List<SurveyAnswer> answersToSave = new ArrayList<>();

        for (SurveySubmissionRequest.AnswerSubmission sub : request.getAnswers()) {
            if (!allowedQuestionIds.contains(sub.getQuestionId())) {
                throw new BadRequestException("Question ID " + sub.getQuestionId() + " is not part of this survey.");
            }

            Question question = questionRepository.findById(sub.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + sub.getQuestionId()));

            // Validation based on type
            if (question.getQuestionType() == QuestionType.RATING && sub.getRating() == null) {
                throw new BadRequestException("Rating is required for question ID: " + question.getId());
            }
            if (question.getQuestionType() == QuestionType.TEXT && (sub.getComment() == null || sub.getComment().trim().isEmpty())) {
                throw new BadRequestException("Comment/Text is required for question ID: " + question.getId());
            }

            SurveyAnswer answer = SurveyAnswer.builder()
                    .response(savedResponse)
                    .question(question)
                    .rating(sub.getRating())
                    .comment(sub.getComment())
                    .build();
            answersToSave.add(answer);
        }

        answerRepository.saveAll(answersToSave);
        log.info("Saved {} answers for survey ID: {} by employee: {}", answersToSave.size(), survey.getId(), employeeId);

        return SubmissionStatusResponse.builder()
                .surveyId(survey.getId())
                .employeeId(employeeId)
                .submitted(true)
                .submittedAt(savedResponse.getSubmittedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionStatusResponse getSubmissionStatus(Long surveyId, String employeeId) {
        return responseRepository.findBySurveyIdAndEmployeeId(surveyId, employeeId)
                .map(res -> SubmissionStatusResponse.builder()
                        .surveyId(surveyId)
                        .employeeId(employeeId)
                        .submitted(true)
                        .submittedAt(res.getSubmittedAt())
                        .build())
                .orElse(SubmissionStatusResponse.builder()
                        .surveyId(surveyId)
                        .employeeId(employeeId)
                        .submitted(false)
                        .submittedAt(null)
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyReportResponse generateSurveyReport(Long surveyId) {
        log.info("Generating survey report for survey ID: {}", surveyId);
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + surveyId));

        long totalResponses = responseRepository.countBySurveyId(surveyId);

        List<SurveyAnswer> answers = answerRepository.findByResponseSurveyId(surveyId);

        // Map containing category average ratings
        Map<String, List<Integer>> categoryRatings = new HashMap<>();
        // Group answers by question ID
        Map<Long, List<SurveyAnswer>> questionAnswers = answers.stream()
                .collect(Collectors.groupingBy(ans -> ans.getQuestion().getId()));

        List<SurveyQuestion> sqList = surveyQuestionRepository.findBySurveyIdOrderByDisplayOrderAsc(surveyId);
        List<SurveyReportResponse.QuestionMetrics> metricsList = new ArrayList<>();

        for (SurveyQuestion sq : sqList) {
            Question q = sq.getQuestion();
            List<SurveyAnswer> qAnswers = questionAnswers.getOrDefault(q.getId(), Collections.emptyList());

            Double avgRating = null;
            Map<Integer, Long> distribution = new HashMap<>();
            List<String> comments = new ArrayList<>();

            if (q.getQuestionType() == QuestionType.RATING) {
                // Initialize distribution map (1-5 to 0)
                for (int val = 1; val <= 5; val++) {
                    distribution.put(val, 0L);
                }

                double sum = 0;
                int count = 0;
                for (SurveyAnswer ans : qAnswers) {
                    if (ans.getRating() != null) {
                        sum += ans.getRating();
                        count++;
                        distribution.put(ans.getRating(), distribution.getOrDefault(ans.getRating(), 0L) + 1);

                        // Accumulate for category ratings
                        categoryRatings.computeIfAbsent(q.getCategory(), k -> new ArrayList<>()).add(ans.getRating());
                    }
                    if (ans.getComment() != null && !ans.getComment().trim().isEmpty()) {
                        comments.add(ans.getComment());
                    }
                }
                if (count > 0) {
                    avgRating = sum / count;
                }
            } else if (q.getQuestionType() == QuestionType.TEXT) {
                for (SurveyAnswer ans : qAnswers) {
                    if (ans.getComment() != null && !ans.getComment().trim().isEmpty()) {
                        comments.add(ans.getComment());
                    }
                }
            }

            metricsList.add(SurveyReportResponse.QuestionMetrics.builder()
                    .questionId(q.getId())
                    .questionText(q.getQuestionText())
                    .category(q.getCategory())
                    .questionType(q.getQuestionType())
                    .averageRating(avgRating)
                    .ratingDistribution(q.getQuestionType() == QuestionType.RATING ? distribution : null)
                    .comments(comments.isEmpty() ? null : comments)
                    .build());
        }

        // Calculate Category Averages
        Map<String, Double> categoryAverages = new HashMap<>();
        categoryRatings.forEach((category, ratingsList) -> {
            double sum = ratingsList.stream().mapToInt(Integer::intValue).sum();
            double avg = sum / ratingsList.size();
            categoryAverages.put(category, avg);
        });

        return SurveyReportResponse.builder()
                .surveyId(survey.getId())
                .title(survey.getTitle())
                .month(survey.getMonth())
                .year(String.valueOf(survey.getYear()))
                .location(survey.getLocation())
                .totalResponses(totalResponses)
                .categoryAverages(categoryAverages)
                .questions(metricsList)
                .build();
    }
}

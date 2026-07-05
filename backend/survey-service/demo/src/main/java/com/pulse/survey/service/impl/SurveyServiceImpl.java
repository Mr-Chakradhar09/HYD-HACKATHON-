package com.pulse.survey.service.impl;

import com.pulse.survey.dto.request.SurveyRequest;
import com.pulse.survey.dto.response.QuestionResponse;
import com.pulse.survey.dto.response.SurveyResponseDto;
import com.pulse.survey.entity.Question;
import com.pulse.survey.entity.Survey;
import com.pulse.survey.entity.SurveyQuestion;
import com.pulse.survey.enums.SurveyStatus;
import com.pulse.survey.exception.BadRequestException;
import com.pulse.survey.exception.ResourceNotFoundException;
import com.pulse.survey.mapper.QuestionMapper;
import com.pulse.survey.mapper.SurveyMapper;
import com.pulse.survey.repository.QuestionRepository;
import com.pulse.survey.repository.SurveyQuestionRepository;
import com.pulse.survey.repository.SurveyRepository;
import com.pulse.survey.service.NotificationProducer;
import com.pulse.survey.service.QuestionVersionService;
import com.pulse.survey.service.SurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final QuestionRepository questionRepository;
    private final SurveyMapper surveyMapper;
    private final QuestionMapper questionMapper;
    private final QuestionVersionService versionService;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public SurveyResponseDto createSurvey(SurveyRequest request) {
        log.info("Creating survey: {} for location: {}", request.getTitle(), request.getLocation());
        Survey survey = surveyMapper.toEntity(request);
        
        survey.setStatus(SurveyStatus.DRAFT);
        if (survey.getVersion() == null) {
            survey.setVersion(versionService.getLatestVersion());
        }

        Survey savedSurvey = surveyRepository.save(survey);

        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            saveSurveyQuestions(savedSurvey, request.getQuestionIds());
        }

        return getSurveyById(savedSurvey.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyResponseDto getSurveyById(Long id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + id));

        List<SurveyQuestion> sqList = surveyQuestionRepository.findBySurveyIdOrderByDisplayOrderAsc(id);
        List<QuestionResponse> questions = sqList.stream()
                .map(sq -> questionMapper.toResponse(sq.getQuestion()))
                .collect(Collectors.toList());

        SurveyResponseDto dto = surveyMapper.toResponseDto(survey);
        dto.setQuestions(questions);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SurveyResponseDto> getAllSurveys(Pageable pageable, String location) {
        Page<Survey> surveys;
        if (location != null && !location.trim().isEmpty()) {
            surveys = surveyRepository.findByLocation(location, pageable);
        } else {
            surveys = surveyRepository.findAll(pageable);
        }

        return surveys.map(survey -> {
            List<SurveyQuestion> sqList = surveyQuestionRepository.findBySurveyIdOrderByDisplayOrderAsc(survey.getId());
            List<QuestionResponse> questions = sqList.stream()
                    .map(sq -> questionMapper.toResponse(sq.getQuestion()))
                    .collect(Collectors.toList());
            SurveyResponseDto dto = surveyMapper.toResponseDto(survey);
            dto.setQuestions(questions);
            return dto;
        });
    }

    @Override
    @Transactional
    public SurveyResponseDto updateSurvey(Long id, SurveyRequest request) {
        log.info("Updating survey ID: {}", id);
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + id));

        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT surveys can be updated. Current status: " + survey.getStatus());
        }

        survey.setTitle(request.getTitle());
        survey.setMonth(request.getMonth());
        survey.setYear(request.getYear());
        survey.setLocation(request.getLocation());
        if (request.getVersion() != null) {
            survey.setVersion(request.getVersion());
        }

        Survey updated = surveyRepository.save(survey);

        if (request.getQuestionIds() != null) {
            surveyQuestionRepository.deleteBySurveyId(id);
            saveSurveyQuestions(updated, request.getQuestionIds());
        }

        return getSurveyById(updated.getId());
    }

    @Override
    @Transactional
    public void deleteSurvey(Long id) {
        log.info("Deleting survey ID: {}", id);
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + id));

        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT surveys can be deleted.");
        }

        surveyQuestionRepository.deleteBySurveyId(id);
        surveyRepository.delete(survey);
        log.info("Successfully deleted survey ID: {}", id);
    }

    @Override
    @Transactional
    public SurveyResponseDto publishSurvey(Long id) {
        log.info("Publishing survey ID: {}", id);
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + id));

        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT surveys can be published.");
        }

        // Verify that the survey has questions
        List<SurveyQuestion> questions = surveyQuestionRepository.findBySurveyIdOrderByDisplayOrderAsc(id);
        if (questions.isEmpty()) {
            throw new BadRequestException("Cannot publish a survey with no questions. Please add questions first.");
        }

        // Deactivate any currently published surveys for this location
        List<Survey> activeSurveys = surveyRepository.findByLocationAndStatus(survey.getLocation(), SurveyStatus.PUBLISHED);
        for (Survey active : activeSurveys) {
            log.info("Closing existing active survey ID: {} for location: {}", active.getId(), active.getLocation());
            active.setStatus(SurveyStatus.CLOSED);
            surveyRepository.save(active);
        }

        survey.setStatus(SurveyStatus.PUBLISHED);
        survey.setPublishedDate(LocalDateTime.now());
        Survey published = surveyRepository.save(survey);

        // Notify employees
        notificationProducer.sendSurveyPublishedNotification(published.getId(), published.getTitle(), published.getLocation());

        return getSurveyById(published.getId());
    }

    @Override
    @Transactional
    public SurveyResponseDto closeSurvey(Long id) {
        log.info("Closing survey ID: {}", id);
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + id));

        if (survey.getStatus() != SurveyStatus.PUBLISHED) {
            throw new BadRequestException("Only PUBLISHED surveys can be closed. Current status: " + survey.getStatus());
        }

        survey.setStatus(SurveyStatus.CLOSED);
        Survey closed = surveyRepository.save(survey);

        // Notify HR
        notificationProducer.sendSurveyClosedNotification(closed.getId(), closed.getTitle(), closed.getLocation());

        return getSurveyById(closed.getId());
    }

    @Override
    @Transactional
    public SurveyResponseDto addQuestionsToSurvey(Long id, List<Long> questionIds) {
        log.info("Adding questions to survey ID: {}", id);
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with ID: " + id));

        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new BadRequestException("Questions can only be added to DRAFT surveys.");
        }

        surveyQuestionRepository.deleteBySurveyId(id);
        if (questionIds != null && !questionIds.isEmpty()) {
            saveSurveyQuestions(survey, questionIds);
        }

        return getSurveyById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyResponseDto getActiveSurveyForLocation(String location, String employeeId) {
        log.info("Fetching active survey for location: {} and employee: {}", location, employeeId);
        List<Survey> activeSurveys = surveyRepository.findByLocationAndStatus(location, SurveyStatus.PUBLISHED);
        if (activeSurveys.isEmpty()) {
            log.info("No active survey found for location: {}", location);
            return null;
        }

        // Return the first active survey found
        Survey active = activeSurveys.get(0);
        SurveyResponseDto dto = getSurveyById(active.getId());
        // All HR-approved AI questions are appended automatically by the Rollout logic, so we just return the DTO.
        return dto;
    }

    @Override
    @Transactional
    public void rolloutSurvey(String location) {
        log.info("Rolling out survey for location: {}", location);
        
        // Find the latest DRAFT survey for this location to use for rollout
        List<Survey> draftSurveys = surveyRepository.findByLocation(location, Pageable.unpaged())
                .stream().filter(s -> s.getStatus() == SurveyStatus.DRAFT).collect(Collectors.toList());
        
        Survey targetSurvey;
        if (draftSurveys.isEmpty()) {
            // If no draft exists, check if there's an active one we can append to, otherwise create a new one.
            List<Survey> activeSurveys = surveyRepository.findByLocationAndStatus(location, SurveyStatus.PUBLISHED);
            if (!activeSurveys.isEmpty()) {
                targetSurvey = activeSurveys.get(0);
                log.info("No draft found. Rolling out directly to active survey ID: {}", targetSurvey.getId());
            } else {
                throw new BadRequestException("No draft or active survey found for location " + location + ". Please create one first.");
            }
        } else {
            targetSurvey = draftSurveys.get(0);
        }
        
        Integer latestVersion = versionService.getLatestVersion();
        List<Question> aiQuestions = questionRepository.findByVersionAndStatus(latestVersion, com.pulse.survey.enums.QuestionStatus.ACTIVE)
                                                       .stream().filter(q -> q.getSource() == com.pulse.survey.enums.QuestionSource.AI)
                                                       .collect(Collectors.toList());
        
        List<SurveyQuestion> existingSq = surveyQuestionRepository.findBySurveyIdOrderByDisplayOrderAsc(targetSurvey.getId());
        List<Long> existingQIds = existingSq.stream().map(sq -> sq.getQuestion().getId()).collect(Collectors.toList());
        
        int order = existingSq.size() + 1;
        List<SurveyQuestion> newSqList = new ArrayList<>();
        for (Question aiQ : aiQuestions) {
            if (!existingQIds.contains(aiQ.getId())) {
                SurveyQuestion sq = SurveyQuestion.builder()
                        .survey(targetSurvey)
                        .question(aiQ)
                        .displayOrder(order++)
                        .build();
                newSqList.add(sq);
            }
        }
        
        if (!newSqList.isEmpty()) {
            surveyQuestionRepository.saveAll(newSqList);
            log.info("Added {} AI questions to survey ID {}", newSqList.size(), targetSurvey.getId());
        }
        
        // If it was a DRAFT, automatically publish it so employees receive it
        if (targetSurvey.getStatus() == SurveyStatus.DRAFT) {
            publishSurvey(targetSurvey.getId());
        } else {
            // Just send a notification that the survey was updated with new questions
            notificationProducer.sendSurveyPublishedNotification(targetSurvey.getId(), targetSurvey.getTitle() + " (Updated)", location);
        }
    }

    private void saveSurveyQuestions(Survey survey, List<Long> questionIds) {
        List<SurveyQuestion> sqList = new ArrayList<>();
        int order = 1;
        for (Long qId : questionIds) {
            Question question = questionRepository.findById(qId)
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + qId));
            
            SurveyQuestion sq = SurveyQuestion.builder()
                    .survey(survey)
                    .question(question)
                    .displayOrder(order++)
                    .build();
            sqList.add(sq);
        }
        surveyQuestionRepository.saveAll(sqList);
        log.info("Linked {} questions to survey ID: {}", sqList.size(), survey.getId());
    }
}

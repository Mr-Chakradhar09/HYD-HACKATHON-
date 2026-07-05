package com.pulse.survey.service;

import com.pulse.survey.dto.request.SurveyRequest;
import com.pulse.survey.dto.response.SurveyResponseDto;
import com.pulse.survey.entity.Survey;
import com.pulse.survey.entity.SurveyQuestion;
import com.pulse.survey.enums.SurveyStatus;
import com.pulse.survey.exception.BadRequestException;
import com.pulse.survey.exception.ResourceNotFoundException;
import com.pulse.survey.mapper.SurveyMapper;
import com.pulse.survey.repository.QuestionRepository;
import com.pulse.survey.repository.SurveyQuestionRepository;
import com.pulse.survey.repository.SurveyRepository;
import com.pulse.survey.service.impl.SurveyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private SurveyQuestionRepository surveyQuestionRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private SurveyMapper surveyMapper;

    @Mock
    private QuestionVersionService versionService;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private SurveyServiceImpl surveyService;

    private SurveyRequest sampleRequest;
    private Survey sampleSurvey;
    private SurveyResponseDto sampleResponseDto;

    @BeforeEach
    void setUp() {
        sampleRequest = SurveyRequest.builder()
                .title("July 2026 Engagement Survey")
                .month("July")
                .year(2026)
                .location("New York")
                .questionIds(Collections.singletonList(1L))
                .build();

        sampleSurvey = Survey.builder()
                .id(1L)
                .title("July 2026 Engagement Survey")
                .month("July")
                .year(2026)
                .location("New York")
                .status(SurveyStatus.DRAFT)
                .version(1)
                .build();

        sampleResponseDto = SurveyResponseDto.builder()
                .id(1L)
                .title("July 2026 Engagement Survey")
                .month("July")
                .year(2026)
                .location("New York")
                .status(SurveyStatus.DRAFT)
                .version(1)
                .questions(new ArrayList<>())
                .build();
    }

    @Test
    void testCreateSurvey() {
        when(surveyMapper.toEntity(any(SurveyRequest.class))).thenReturn(sampleSurvey);
        when(versionService.getLatestVersion()).thenReturn(1);
        when(surveyRepository.save(any(Survey.class))).thenReturn(sampleSurvey);
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(sampleSurvey));
        when(surveyQuestionRepository.findBySurveyIdOrderByDisplayOrderAsc(1L)).thenReturn(new ArrayList<>());
        when(surveyMapper.toResponseDto(any(Survey.class))).thenReturn(sampleResponseDto);

        SurveyResponseDto result = surveyService.createSurvey(sampleRequest);

        assertNotNull(result);
        assertEquals("July 2026 Engagement Survey", result.getTitle());
        assertEquals(SurveyStatus.DRAFT, result.getStatus());
        verify(surveyRepository, times(1)).save(any(Survey.class));
    }

    @Test
    void testPublishSurvey_NoQuestions() {
        when(surveyRepository.findById(1L)).thenReturn(Optional.of(sampleSurvey));
        // Mocking survey having no questions linked
        when(surveyQuestionRepository.findBySurveyIdOrderByDisplayOrderAsc(1L)).thenReturn(new ArrayList<>());

        assertThrows(BadRequestException.class, () -> surveyService.publishSurvey(1L));
        verify(surveyRepository, never()).save(any(Survey.class));
    }
}

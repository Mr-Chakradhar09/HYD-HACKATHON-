package com.pulse.survey.service;

import com.pulse.survey.dto.request.QuestionRequest;
import com.pulse.survey.dto.response.QuestionResponse;
import com.pulse.survey.entity.Question;
import com.pulse.survey.enums.QuestionStatus;
import com.pulse.survey.enums.QuestionSource;
import com.pulse.survey.enums.QuestionType;
import com.pulse.survey.mapper.QuestionMapper;
import com.pulse.survey.repository.QuestionRepository;
import com.pulse.survey.service.impl.QuestionBankServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionBankServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private QuestionVersionService versionService;

    @InjectMocks
    private QuestionBankServiceImpl questionBankService;

    private QuestionRequest sampleRequest;
    private Question sampleQuestion;
    private QuestionResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleRequest = QuestionRequest.builder()
                .questionText("Sample Question text")
                .category("ENGAGEMENT")
                .questionType(QuestionType.RATING)
                .build();

        sampleQuestion = Question.builder()
                .id(1L)
                .questionText("Sample Question text")
                .category("ENGAGEMENT")
                .questionType(QuestionType.RATING)
                .source(QuestionSource.STATIC)
                .status(QuestionStatus.ACTIVE)
                .version(1)
                .build();

        sampleResponse = QuestionResponse.builder()
                .id(1L)
                .questionText("Sample Question text")
                .category("ENGAGEMENT")
                .questionType(QuestionType.RATING)
                .source(QuestionSource.STATIC)
                .status(QuestionStatus.ACTIVE)
                .version(1)
                .build();
    }

    @Test
    void testCreateQuestion() {
        when(questionMapper.toEntity(any(QuestionRequest.class))).thenReturn(sampleQuestion);
        when(versionService.getLatestVersion()).thenReturn(1);
        when(questionRepository.save(any(Question.class))).thenReturn(sampleQuestion);
        when(questionMapper.toResponse(any(Question.class))).thenReturn(sampleResponse);

        QuestionResponse response = questionBankService.createQuestion(sampleRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Sample Question text", response.getQuestionText());
        assertEquals(QuestionSource.STATIC, response.getSource());
        assertEquals(QuestionStatus.ACTIVE, response.getStatus());

        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    void testGetQuestionById_Success() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(sampleQuestion));
        when(questionMapper.toResponse(sampleQuestion)).thenReturn(sampleResponse);

        QuestionResponse response = questionBankService.getQuestionById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Sample Question text", response.getQuestionText());

        verify(questionRepository, times(1)).findById(1L);
    }

    @Test
    void testGetQuestionById_NotFound() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> questionBankService.getQuestionById(99L));

        verify(questionRepository, times(1)).findById(99L);
    }
}

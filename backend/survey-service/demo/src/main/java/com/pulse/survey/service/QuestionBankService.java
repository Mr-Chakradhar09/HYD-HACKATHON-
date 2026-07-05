package com.pulse.survey.service;

import com.pulse.survey.dto.request.QuestionRequest;
import com.pulse.survey.dto.response.QuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuestionBankService {
    QuestionResponse createQuestion(QuestionRequest request);
    QuestionResponse getQuestionById(Long id);
    Page<QuestionResponse> getAllQuestions(Pageable pageable);
    QuestionResponse updateQuestion(Long id, QuestionRequest request);
    void deleteQuestion(Long id);
    List<QuestionResponse> getQuestionsByVersion(Integer version);
}

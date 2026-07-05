package com.pulse.survey.service;

import com.pulse.survey.dto.request.DraftQuestionRequest;
import com.pulse.survey.dto.response.DraftQuestionResponse;
import com.pulse.survey.enums.DraftQuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DraftQuestionService {
    Page<DraftQuestionResponse> getDraftQuestions(Pageable pageable, DraftQuestionStatus status);
    DraftQuestionResponse editDraftQuestion(Long id, DraftQuestionRequest request);
}

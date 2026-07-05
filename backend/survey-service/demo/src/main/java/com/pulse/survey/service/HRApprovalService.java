package com.pulse.survey.service;

import com.pulse.survey.dto.response.QuestionResponse;

public interface HRApprovalService {
    QuestionResponse approveDraftQuestion(Long id);
    void rejectDraftQuestion(Long id);
}

package com.pulse.survey.service;

import com.pulse.survey.dto.request.SurveySubmissionRequest;
import com.pulse.survey.dto.response.SubmissionStatusResponse;
import com.pulse.survey.dto.response.SurveyReportResponse;

public interface ResponseService {
    SubmissionStatusResponse submitSurveyResponse(SurveySubmissionRequest request, String employeeId);
    SubmissionStatusResponse getSubmissionStatus(Long surveyId, String employeeId);
    SurveyReportResponse generateSurveyReport(Long surveyId);
}

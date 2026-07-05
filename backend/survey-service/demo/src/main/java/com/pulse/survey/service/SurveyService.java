package com.pulse.survey.service;

import com.pulse.survey.dto.request.SurveyRequest;
import com.pulse.survey.dto.response.SurveyResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SurveyService {
    SurveyResponseDto createSurvey(SurveyRequest request);
    SurveyResponseDto getSurveyById(Long id);
    Page<SurveyResponseDto> getAllSurveys(Pageable pageable, String location);
    SurveyResponseDto updateSurvey(Long id, SurveyRequest request);
    void deleteSurvey(Long id);
    SurveyResponseDto publishSurvey(Long id);
    SurveyResponseDto closeSurvey(Long id);
    SurveyResponseDto addQuestionsToSurvey(Long id, List<Long> questionIds);
    SurveyResponseDto getActiveSurveyForLocation(String location, String employeeId);
    void rolloutSurvey(String location);
}

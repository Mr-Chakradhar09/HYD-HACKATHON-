package com.pulse.survey.repository;

import com.pulse.survey.entity.SurveyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    Optional<SurveyResponse> findBySurveyIdAndEmployeeId(Long surveyId, String employeeId);

    List<SurveyResponse> findBySurveyId(Long surveyId);

    Page<SurveyResponse> findBySurveyId(Long surveyId, Pageable pageable);

    long countBySurveyId(Long surveyId);

    boolean existsBySurveyIdAndEmployeeId(Long surveyId, String employeeId);
}

package com.pulse.survey.repository;

import com.pulse.survey.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    List<SurveyQuestion> findBySurveyIdOrderByDisplayOrderAsc(Long surveyId);

    void deleteBySurveyId(Long surveyId);
}

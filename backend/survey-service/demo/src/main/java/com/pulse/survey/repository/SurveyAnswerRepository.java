package com.pulse.survey.repository;

import com.pulse.survey.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    List<SurveyAnswer> findByResponseId(Long responseId);

    List<SurveyAnswer> findByResponseSurveyId(Long surveyId);
}

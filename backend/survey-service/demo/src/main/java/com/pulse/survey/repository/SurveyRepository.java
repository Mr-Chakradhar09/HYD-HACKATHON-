package com.pulse.survey.repository;

import com.pulse.survey.entity.Survey;
import com.pulse.survey.enums.SurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    List<Survey> findByLocationAndStatus(String location, SurveyStatus status);

    Page<Survey> findByLocation(String location, Pageable pageable);

    Optional<Survey> findByIdAndLocation(Long id, String location);
}

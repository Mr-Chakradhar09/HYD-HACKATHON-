package com.virtusa.pulse.ai.repository;

import com.virtusa.pulse.ai.entity.SentimentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentimentReportRepository extends JpaRepository<SentimentReport, Long> {
    List<SentimentReport> findByLocation(String location);
    List<SentimentReport> findBySurveyId(Long surveyId);
}

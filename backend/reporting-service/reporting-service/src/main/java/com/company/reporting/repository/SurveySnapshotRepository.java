package com.company.reporting.repository;

import com.company.reporting.entity.SurveySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SurveySnapshotRepository extends JpaRepository<SurveySnapshot, Long> {
    List<SurveySnapshot> findByMonth(String month);
    List<SurveySnapshot> findBySurveyId(Long surveyId);
    List<SurveySnapshot> findByEmployeeId(Long employeeId);
    List<SurveySnapshot> findByManagerId(Long managerId);
    
    @Query("SELECT COUNT(s) FROM SurveySnapshot s WHERE s.month = :month")
    long countByMonth(@Param("month") String month);

    @Query("SELECT s.city, AVG(s.sentimentScore), COUNT(s) FROM SurveySnapshot s WHERE s.month = :month GROUP BY s.city")
    List<Object[]> getCityAggregates(@Param("month") String month);

    @Query("SELECT s.department, AVG(s.sentimentScore), COUNT(s) FROM SurveySnapshot s WHERE s.month = :month GROUP BY s.department")
    List<Object[]> getDepartmentAggregates(@Param("month") String month);

    @Query("SELECT s.theme, AVG(s.sentimentScore), COUNT(s) FROM SurveySnapshot s WHERE s.month = :month GROUP BY s.theme")
    List<Object[]> getThemeAggregates(@Param("month") String month);
}

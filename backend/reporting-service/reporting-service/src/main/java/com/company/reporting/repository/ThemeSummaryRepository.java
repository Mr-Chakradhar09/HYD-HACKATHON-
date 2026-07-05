package com.company.reporting.repository;

import com.company.reporting.entity.ThemeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThemeSummaryRepository extends JpaRepository<ThemeSummary, Long> {
    Optional<ThemeSummary> findByThemeAndMonth(String theme, String month);
    List<ThemeSummary> findByMonth(String month);
    List<ThemeSummary> findByThemeIgnoreCase(String theme);
}

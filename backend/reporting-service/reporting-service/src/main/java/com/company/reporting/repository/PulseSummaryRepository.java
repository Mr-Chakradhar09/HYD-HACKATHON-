package com.company.reporting.repository;

import com.company.reporting.entity.PulseSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PulseSummaryRepository extends JpaRepository<PulseSummary, Long> {
    Optional<PulseSummary> findByMonth(String month);
}

package com.company.reporting.repository;

import com.company.reporting.entity.AIInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AIInsightRepository extends JpaRepository<AIInsight, Long> {
    Optional<AIInsight> findByMonth(String month);
}

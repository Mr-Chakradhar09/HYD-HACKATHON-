package com.company.reporting.scheduler;

import com.company.reporting.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlySnapshotScheduler {

    private final AnalyticsService analyticsService;

    // Run at 1 AM on the first day of every month to aggregate previous month
    @Scheduled(cron = "0 0 1 1 * ?")
    public void schedulePreviousMonthAggregation() {
        String prevMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("Scheduled task: triggering aggregation for previous month: {}", prevMonth);
        try {
            analyticsService.aggregateMonthlyData(prevMonth);
        } catch (Exception e) {
            log.error("Failed to run scheduled previous month aggregation: {}", e.getMessage());
        }
    }

    // Run daily at midnight to update current month's running aggregates
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleCurrentMonthAggregation() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        log.info("Scheduled task: refreshing aggregation for current month: {}", currentMonth);
        try {
            analyticsService.aggregateMonthlyData(currentMonth);
        } catch (Exception e) {
            log.error("Failed to run scheduled current month aggregation: {}", e.getMessage());
        }
    }
}

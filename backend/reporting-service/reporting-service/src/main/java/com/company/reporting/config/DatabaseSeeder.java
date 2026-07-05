package com.company.reporting.config;

import com.company.reporting.entity.SurveySnapshot;
import com.company.reporting.repository.SurveySnapshotRepository;
import com.company.reporting.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final SurveySnapshotRepository surveySnapshotRepository;
    private final AnalyticsService analyticsService;

    @org.springframework.beans.factory.annotation.Value("${app.seeding.enabled:true}")
    private boolean seedingEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (!seedingEnabled) {
            log.info("Database seeding is disabled by configuration.");
            return;
        }
        if (surveySnapshotRepository.count() == 0) {
            log.info("Database is empty. Seeding one manual dummy survey snapshot record.");
            SurveySnapshot snapshot = SurveySnapshot.builder()
                    .employeeId(1001L)
                    .surveyId(101L)
                    .city("Chennai")
                    .state("Tamil Nadu")
                    .country("India")
                    .region("APAC")
                    .businessUnit("BFSI")
                    .department("Engineering")
                    .managerId(501L)
                    .theme("Work-Life Balance")
                    .sentiment("POSITIVE")
                    .sentimentScore(85.0)
                    .burnoutRisk("LOW")
                    .month("2026-07")
                    .submittedAt(LocalDateTime.now())
                    .build();

            // Use analyticsService so that aggregates are compiled and generated automatically
            analyticsService.processSurveySubmission(snapshot);
            log.info("Successfully seeded database with one survey snapshot and compiled aggregates.");
        } else {
            log.info("Database already contains records. Skipping seed script.");
        }
    }
}

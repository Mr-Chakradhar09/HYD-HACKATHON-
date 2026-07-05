package com.company.reporting.service;

import com.company.reporting.entity.*;
import com.company.reporting.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "app.seeding.enabled=false")
@ActiveProfiles("dev")
@Transactional
public class AnalyticsServiceTests {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private SurveySnapshotRepository surveySnapshotRepository;

    @Autowired
    private PulseSummaryRepository pulseSummaryRepository;

    @Autowired
    private LocationSummaryRepository locationSummaryRepository;

    @Autowired
    private ThemeSummaryRepository themeSummaryRepository;

    @Autowired
    private AIInsightRepository aiInsightRepository;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        surveySnapshotRepository.deleteAll();
        pulseSummaryRepository.deleteAll();
        locationSummaryRepository.deleteAll();
        themeSummaryRepository.deleteAll();
        aiInsightRepository.deleteAll();
    }

    @Test
    void testMonthlyAggregationCalculations() {
        String month = "2026-07";

        // Create 4 mock snapshots (3 POSITIVE, 1 NEGATIVE) -> Pulse score should be 75%
        SurveySnapshot snapshot1 = SurveySnapshot.builder()
                .employeeId(1L)
                .surveyId(10L)
                .city("Chennai")
                .state("Tamil Nadu")
                .country("India")
                .region("APAC")
                .theme("Work-Life Balance")
                .sentiment("POSITIVE")
                .sentimentScore(85.0)
                .month(month)
                .submittedAt(LocalDateTime.now())
                .build();

        SurveySnapshot snapshot2 = SurveySnapshot.builder()
                .employeeId(2L)
                .surveyId(10L)
                .city("Chennai")
                .state("Tamil Nadu")
                .country("India")
                .region("APAC")
                .theme("Work-Life Balance")
                .sentiment("POSITIVE")
                .sentimentScore(90.0)
                .month(month)
                .submittedAt(LocalDateTime.now())
                .build();

        SurveySnapshot snapshot3 = SurveySnapshot.builder()
                .employeeId(3L)
                .surveyId(10L)
                .city("Pune")
                .state("Maharashtra")
                .country("India")
                .region("APAC")
                .theme("Career Growth")
                .sentiment("POSITIVE")
                .sentimentScore(80.0)
                .month(month)
                .submittedAt(LocalDateTime.now())
                .build();

        SurveySnapshot snapshot4 = SurveySnapshot.builder()
                .employeeId(4L)
                .surveyId(10L)
                .city("Pune")
                .state("Maharashtra")
                .country("India")
                .region("APAC")
                .theme("Work-Life Balance")
                .sentiment("NEGATIVE")
                .sentimentScore(40.0)
                .month(month)
                .submittedAt(LocalDateTime.now())
                .build();

        surveySnapshotRepository.saveAll(List.of(snapshot1, snapshot2, snapshot3, snapshot4));

        // Perform aggregation
        analyticsService.aggregateMonthlyData(month);

        // 1. Assert PulseSummary (3 positive out of 4 total -> 75.0%)
        PulseSummary pulseSummary = pulseSummaryRepository.findByMonth(month).orElse(null);
        assertNotNull(pulseSummary);
        assertEquals(75.0, pulseSummary.getPulseScore());
        assertEquals(4, pulseSummary.getTotalResponses());
        assertEquals(75.0, pulseSummary.getPositiveSentiment());
        assertEquals(25.0, pulseSummary.getNegativeSentiment());

        // 2. Assert LocationSummary
        // Chennai has 2 POSITIVE -> 100.0%
        LocationSummary chennaiSummary = locationSummaryRepository.findByLocationNameAndLocationTypeAndMonth("Chennai", "CITY", month).orElse(null);
        assertNotNull(chennaiSummary);
        assertEquals(100.0, chennaiSummary.getScore());
        assertEquals(2, chennaiSummary.getTotalResponses());

        // Pune has 1 POSITIVE, 1 NEGATIVE -> 50.0%
        LocationSummary puneSummary = locationSummaryRepository.findByLocationNameAndLocationTypeAndMonth("Pune", "CITY", month).orElse(null);
        assertNotNull(puneSummary);
        assertEquals(50.0, puneSummary.getScore());
        assertEquals(2, puneSummary.getTotalResponses());

        // India has 3 POSITIVE, 1 NEGATIVE -> 75.0%
        LocationSummary indiaSummary = locationSummaryRepository.findByLocationNameAndLocationTypeAndMonth("India", "COUNTRY", month).orElse(null);
        assertNotNull(indiaSummary);
        assertEquals(75.0, indiaSummary.getScore());

        // 3. Assert ThemeSummary
        // Work-Life Balance: 2 POSITIVE, 1 NEGATIVE -> 66.67%
        ThemeSummary wlbSummary = themeSummaryRepository.findByThemeAndMonth("Work-Life Balance", month).orElse(null);
        assertNotNull(wlbSummary);
        assertEquals(2.0/3.0 * 100, wlbSummary.getScore(), 0.01);

        // Career Growth: 1 POSITIVE -> 100%
        ThemeSummary cgSummary = themeSummaryRepository.findByThemeAndMonth("Career Growth", month).orElse(null);
        assertNotNull(cgSummary);
        assertEquals(100.0, cgSummary.getScore());

        // 4. Assert AI Insight was generated
        AIInsight aiInsight = aiInsightRepository.findByMonth(month).orElse(null);
        assertNotNull(aiInsight);
        assertTrue(aiInsight.getInsight().contains("Work-Life Balance"));
    }
}

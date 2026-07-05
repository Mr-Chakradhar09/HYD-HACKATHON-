package com.company.reporting.service.impl;

import com.company.reporting.entity.*;
import com.company.reporting.repository.*;
import com.company.reporting.service.AnalyticsService;
import com.company.reporting.service.InsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final SurveySnapshotRepository surveySnapshotRepository;
    private final PulseSummaryRepository pulseSummaryRepository;
    private final LocationSummaryRepository locationSummaryRepository;
    private final ThemeSummaryRepository themeSummaryRepository;
    private final InsightService insightService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    public void processSurveySubmission(SurveySnapshot snapshot) {
        log.info("Processing survey submission for employee: {} in month: {}", snapshot.getEmployeeId(), snapshot.getMonth());
        surveySnapshotRepository.save(snapshot);
        // Automatically trigger monthly aggregation updates
        aggregateMonthlyData(snapshot.getMonth());
    }

    @Override
    @Transactional
    public void aggregateMonthlyData(String month) {
        log.info("Starting aggregation for month: {}", month);
        List<SurveySnapshot> snapshots = surveySnapshotRepository.findByMonth(month);
        if (snapshots.isEmpty()) {
            log.warn("No survey snapshots found for month: {}", month);
            return;
        }

        int totalResponses = snapshots.size();

        // 1. Calculate overall sentiment counts
        long positiveCount = snapshots.stream()
                .filter(s -> "POSITIVE".equalsIgnoreCase(s.getSentiment()))
                .count();
        long neutralCount = snapshots.stream()
                .filter(s -> "NEUTRAL".equalsIgnoreCase(s.getSentiment()))
                .count();
        long negativeCount = snapshots.stream()
                .filter(s -> "NEGATIVE".equalsIgnoreCase(s.getSentiment()))
                .count();

        double positivePct = totalResponses > 0 ? (double) positiveCount / totalResponses * 100 : 0.0;
        double neutralPct = totalResponses > 0 ? (double) neutralCount / totalResponses * 100 : 0.0;
        double negativePct = totalResponses > 0 ? (double) negativeCount / totalResponses * 100 : 0.0;

        // Pulse Score = (Positive / Total) * 100
        double overallPulseScore = positivePct;

        // Participation Rate calculation: Target 5000 employees for hackathon setup
        double participationRate = Math.min(100.0, ((double) totalResponses / 5000.0) * 100.0);

        // Update PulseSummary
        PulseSummary pulseSummary = pulseSummaryRepository.findByMonth(month)
                .orElse(PulseSummary.builder().month(month).build());
        pulseSummary.setPulseScore(overallPulseScore);
        pulseSummary.setParticipationRate(participationRate);
        pulseSummary.setPositiveSentiment(positivePct);
        pulseSummary.setNeutralSentiment(neutralPct);
        pulseSummary.setNegativeSentiment(negativePct);
        pulseSummary.setTotalResponses(totalResponses);
        pulseSummaryRepository.save(pulseSummary);

        // 2. Location level aggregates
        // We aggregate by City, State, Country, Region
        Map<String, List<SurveySnapshot>> byCity = snapshots.stream()
                .filter(s -> s.getCity() != null)
                .collect(Collectors.groupingBy(SurveySnapshot::getCity));
        updateLocationSummaries(byCity, "CITY", month);

        Map<String, List<SurveySnapshot>> byState = snapshots.stream()
                .filter(s -> s.getState() != null)
                .collect(Collectors.groupingBy(SurveySnapshot::getState));
        updateLocationSummaries(byState, "STATE", month);

        Map<String, List<SurveySnapshot>> byCountry = snapshots.stream()
                .filter(s -> s.getCountry() != null)
                .collect(Collectors.groupingBy(SurveySnapshot::getCountry));
        updateLocationSummaries(byCountry, "COUNTRY", month);

        Map<String, List<SurveySnapshot>> byRegion = snapshots.stream()
                .filter(s -> s.getRegion() != null)
                .collect(Collectors.groupingBy(SurveySnapshot::getRegion));
        updateLocationSummaries(byRegion, "REGION", month);

        // 3. Theme level aggregates
        Map<String, List<SurveySnapshot>> byTheme = snapshots.stream()
                .filter(s -> s.getTheme() != null)
                .collect(Collectors.groupingBy(SurveySnapshot::getTheme));
        for (Map.Entry<String, List<SurveySnapshot>> entry : byTheme.entrySet()) {
            String theme = entry.getKey();
            List<SurveySnapshot> themeSnapshots = entry.getValue();
            long themePosCount = themeSnapshots.stream()
                    .filter(s -> "POSITIVE".equalsIgnoreCase(s.getSentiment()))
                    .count();
            double themeScore = themeSnapshots.isEmpty() ? 0.0 : ((double) themePosCount / themeSnapshots.size()) * 100.0;

            ThemeSummary themeSummary = themeSummaryRepository.findByThemeAndMonth(theme, month)
                    .orElse(ThemeSummary.builder().theme(theme).month(month).build());
            themeSummary.setScore(themeScore);
            themeSummary.setTotalResponses(themeSnapshots.size());
            themeSummaryRepository.save(themeSummary);
        }

        // 4. Generate AI Insights heuristic or model based
        insightService.generateAIInsights(month);

        // 5. Send notification via Kafka
        sendReportNotification(month, overallPulseScore);
    }

    private void updateLocationSummaries(Map<String, List<SurveySnapshot>> grouping, String type, String month) {
        for (Map.Entry<String, List<SurveySnapshot>> entry : grouping.entrySet()) {
            String locName = entry.getKey();
            List<SurveySnapshot> locSnapshots = entry.getValue();
            long locPosCount = locSnapshots.stream()
                    .filter(s -> "POSITIVE".equalsIgnoreCase(s.getSentiment()))
                    .count();
            double locScore = locSnapshots.isEmpty() ? 0.0 : ((double) locPosCount / locSnapshots.size()) * 100.0;

            LocationSummary locSummary = locationSummaryRepository.findByLocationNameAndLocationTypeAndMonth(locName, type, month)
                    .orElse(LocationSummary.builder().locationName(locName).locationType(type).month(month).build());
            locSummary.setScore(locScore);
            locSummary.setTotalResponses(locSnapshots.size());
            locSummary.setParticipationRate(Math.min(100.0, ((double) locSnapshots.size() / 1000.0) * 100.0)); // assume target 1000 per location type
            locationSummaryRepository.save(locSummary);
        }
    }

    private void sendReportNotification(String month, double score) {
        String topic = "notification-topic";
        String message = String.format("{\"event\":\"REPORT_GENERATED\",\"month\":\"%s\",\"pulseScore\":%.2f,\"timestamp\":\"%s\"}", 
                month, score, java.time.LocalDateTime.now());
        try {
            kafkaTemplate.send(topic, message);
            log.info("Notification successfully sent to Kafka topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send notification event via Kafka. Running in standalone mode: {}", e.getMessage());
        }
    }
}

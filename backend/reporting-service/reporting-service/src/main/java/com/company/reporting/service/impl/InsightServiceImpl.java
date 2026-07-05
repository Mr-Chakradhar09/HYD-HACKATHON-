package com.company.reporting.service.impl;

import com.company.reporting.dto.AIInsightDTO;
import com.company.reporting.entity.AIInsight;
import com.company.reporting.entity.LocationSummary;
import com.company.reporting.entity.ThemeSummary;
import com.company.reporting.exception.ReportNotFoundException;
import com.company.reporting.mapper.ReportMapper;
import com.company.reporting.repository.AIInsightRepository;
import com.company.reporting.repository.LocationSummaryRepository;
import com.company.reporting.repository.ThemeSummaryRepository;
import com.company.reporting.service.InsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsightServiceImpl implements InsightService {

    private final AIInsightRepository aiInsightRepository;
    private final ThemeSummaryRepository themeSummaryRepository;
    private final LocationSummaryRepository locationSummaryRepository;
    private final ReportMapper reportMapper;

    @Override
    public AIInsightDTO getInsightByMonth(String month) {
        AIInsight insight = aiInsightRepository.findByMonth(month)
                .orElseThrow(() -> new ReportNotFoundException("AI Insights not found for month: " + month));
        return reportMapper.toAIInsightDTO(insight);
    }

    @Override
    public List<String> getTopConcerns(String month) {
        AIInsightDTO insight = getInsightByMonth(month);
        return insight.getTopConcerns();
    }

    @Override
    public String getSentimentSummary(String month) {
        AIInsightDTO insight = getInsightByMonth(month);
        return insight.getSummary();
    }

    @Override
    public String getLocationAnalysis(String month) {
        AIInsightDTO insight = getInsightByMonth(month);
        return insight.getLocationAnalysis();
    }

    @Override
    @Transactional
    public void generateAIInsights(String month) {
        log.info("Generating AI Insights for month: {}", month);

        List<ThemeSummary> themes = themeSummaryRepository.findByMonth(month);
        List<LocationSummary> locations = locationSummaryRepository.findByMonth(month);

        // Calculate top concerns (lowest scoring themes)
        List<String> concerns = themes.stream()
                .sorted(Comparator.comparing(ThemeSummary::getScore))
                .limit(3)
                .map(ThemeSummary::getTheme)
                .collect(Collectors.toList());

        String topConcernsStr = String.join(",", concerns);

        // Find best and worst performing locations
        List<LocationSummary> cities = locations.stream()
                .filter(l -> "CITY".equalsIgnoreCase(l.getLocationType()))
                .sorted(Comparator.comparing(LocationSummary::getScore))
                .collect(Collectors.toList());

        String bestCity = cities.isEmpty() ? "N/A" : cities.get(cities.size() - 1).getLocationName();
        double bestScore = cities.isEmpty() ? 0.0 : cities.get(cities.size() - 1).getScore();
        String worstCity = cities.isEmpty() ? "N/A" : cities.get(0).getLocationName();
        double worstScore = cities.isEmpty() ? 0.0 : cities.get(0).getScore();

        // Draft summaries
        String summary = String.format(
                "Overall employee sentiment is showing steady trends this month. The key concern areas impacting engagement are %s. HR interventions around these themes are highly recommended.",
                concerns.isEmpty() ? "none" : String.join(" and ", concerns)
        );

        String locationAnalysis = String.format(
                "Location audit indicates high engagement in %s with an average score of %.1f%%. Conversely, employee morale in %s requires attention as it scored %.1f%%.",
                bestCity, bestScore, worstCity, worstScore
        );

        AIInsight insight = aiInsightRepository.findByMonth(month)
                .orElse(AIInsight.builder().month(month).build());
        
        insight.setInsight(summary);
        insight.setTopConcerns(topConcernsStr);
        insight.setLocationAnalysis(locationAnalysis);

        aiInsightRepository.save(insight);
        log.info("Saved AI Insights for month: {}", month);
    }
}

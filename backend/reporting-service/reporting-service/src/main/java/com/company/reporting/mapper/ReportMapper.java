package com.company.reporting.mapper;

import com.company.reporting.dto.AIInsightDTO;
import com.company.reporting.dto.LocationReportDTO;
import com.company.reporting.dto.ThemeReportDTO;
import com.company.reporting.entity.AIInsight;
import com.company.reporting.entity.LocationSummary;
import com.company.reporting.entity.ThemeSummary;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportMapper {

    public LocationReportDTO toLocationReportDTO(LocationSummary summary) {
        if (summary == null) return null;
        return LocationReportDTO.builder()
                .location(summary.getLocationName())
                .locationType(summary.getLocationType())
                .pulseScore(summary.getScore())
                .responseRate(summary.getParticipationRate())
                .totalResponses(summary.getTotalResponses())
                .build();
    }

    public ThemeReportDTO toThemeReportDTO(ThemeSummary summary) {
        if (summary == null) return null;
        return ThemeReportDTO.builder()
                .theme(summary.getTheme())
                .score(summary.getScore())
                .totalResponses(summary.getTotalResponses())
                .build();
    }

    public AIInsightDTO toAIInsightDTO(AIInsight insight) {
        if (insight == null) return null;
        List<String> concerns = insight.getTopConcerns() != null && !insight.getTopConcerns().isEmpty()
                ? Arrays.stream(insight.getTopConcerns().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return AIInsightDTO.builder()
                .month(insight.getMonth())
                .summary(insight.getInsight())
                .topConcerns(concerns)
                .locationAnalysis(insight.getLocationAnalysis())
                .build();
    }
}

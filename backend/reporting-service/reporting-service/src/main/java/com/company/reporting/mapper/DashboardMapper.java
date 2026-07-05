package com.company.reporting.mapper;

import com.company.reporting.dto.DashboardResponseDTO;
import com.company.reporting.entity.PulseSummary;
import org.springframework.stereotype.Component;

@Component
public class DashboardMapper {

    public DashboardResponseDTO toDashboardResponseDTO(PulseSummary summary, String topConcern, String highestRatedLocation) {
        if (summary == null) {
            return DashboardResponseDTO.builder()
                    .pulseScore(0.0)
                    .participationRate(0.0)
                    .positiveSentiment(0.0)
                    .neutralSentiment(0.0)
                    .negativeSentiment(0.0)
                    .totalResponses(0)
                    .topConcern(topConcern != null ? topConcern : "N/A")
                    .highestRatedLocation(highestRatedLocation != null ? highestRatedLocation : "N/A")
                    .build();
        }

        return DashboardResponseDTO.builder()
                .pulseScore(summary.getPulseScore())
                .participationRate(summary.getParticipationRate())
                .positiveSentiment(summary.getPositiveSentiment())
                .neutralSentiment(summary.getNeutralSentiment())
                .negativeSentiment(summary.getNegativeSentiment())
                .totalResponses(summary.getTotalResponses())
                .topConcern(topConcern != null ? topConcern : "N/A")
                .highestRatedLocation(highestRatedLocation != null ? highestRatedLocation : "N/A")
                .build();
    }
}

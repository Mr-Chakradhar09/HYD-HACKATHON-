package com.company.reporting.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDTO {
    private Double pulseScore;
    private Double participationRate;
    private Double positiveSentiment;
    private Double neutralSentiment;
    private Double negativeSentiment;
    private Integer totalResponses;
    private String topConcern;
    private String highestRatedLocation;
}

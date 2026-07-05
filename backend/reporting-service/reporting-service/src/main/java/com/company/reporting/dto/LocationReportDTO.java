package com.company.reporting.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationReportDTO {
    private String location;
    private String locationType;
    private Double pulseScore;
    private Double responseRate;
    private Integer totalResponses;
}

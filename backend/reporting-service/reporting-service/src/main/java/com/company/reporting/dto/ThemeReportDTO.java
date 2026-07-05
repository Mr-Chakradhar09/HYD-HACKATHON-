package com.company.reporting.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeReportDTO {
    private String theme;
    private Double score;
    private Integer totalResponses;
}

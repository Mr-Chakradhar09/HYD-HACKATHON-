package com.company.reporting.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendDTO {
    private String month;
    private Double pulseScore;
}

package com.company.reporting.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIInsightDTO {
    private String month;
    private String summary;
    private List<String> topConcerns;
    private String locationAnalysis;
}

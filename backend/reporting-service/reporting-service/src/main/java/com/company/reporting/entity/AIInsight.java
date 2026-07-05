package com.company.reporting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_insight")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIInsight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_month", nullable = false, unique = true)
    private String month; // e.g. "2026-07"

    @Lob
    @Column(columnDefinition = "TEXT")
    private String insight; // The AI generated summary text

    @Column(name = "top_concerns")
    private String topConcerns; // Comma separated theme names, e.g. "Work-Life Balance,Career Growth"

    @Lob
    @Column(name = "location_analysis", columnDefinition = "TEXT")
    private String locationAnalysis; // AI location specific analysis
}

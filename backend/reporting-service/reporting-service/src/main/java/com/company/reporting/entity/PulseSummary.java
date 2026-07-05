package com.company.reporting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pulse_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PulseSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_month", nullable = false, unique = true)
    private String month; // e.g. "2026-07"

    @Column(name = "pulse_score")
    private Double pulseScore;

    @Column(name = "participation_rate")
    private Double participationRate;

    @Column(name = "positive_sentiment")
    private Double positiveSentiment;

    @Column(name = "neutral_sentiment")
    private Double neutralSentiment;

    @Column(name = "negative_sentiment")
    private Double negativeSentiment;

    @Column(name = "total_responses")
    private Integer totalResponses;
}

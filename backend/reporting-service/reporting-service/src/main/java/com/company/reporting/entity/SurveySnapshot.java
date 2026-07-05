package com.company.reporting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "survey_snapshot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveySnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "survey_id")
    private Long surveyId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    private String city;
    private String state;
    private String country;
    private String region;

    @Column(name = "business_unit")
    private String businessUnit;

    private String department;

    @Column(name = "manager_id")
    private Long managerId;

    private String theme;

    private String sentiment; // "POSITIVE", "NEUTRAL", "NEGATIVE"

    @Column(name = "sentiment_score")
    private Double sentimentScore;

    @Column(name = "burnout_risk")
    private String burnoutRisk; // "LOW", "MEDIUM", "HIGH"

    @Column(name = "report_month", nullable = false)
    private String month; // e.g. "2026-07"

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}

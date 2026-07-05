package com.company.reporting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "theme_summary", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"theme", "report_month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String theme; // e.g. "Work-Life Balance", "Career Growth", "Leadership"

    @Column(name = "report_month", nullable = false)
    private String month; // e.g. "2026-07"

    @Column(name = "score")
    private Double score;

    @Column(name = "total_responses")
    private Integer totalResponses;
}

package com.company.reporting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "location_summary", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"location_name", "location_type", "report_month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_name", nullable = false)
    private String locationName; // e.g. "Chennai", "Maharashtra", "India", "APAC"

    @Column(name = "location_type", nullable = false)
    private String locationType; // "CITY", "STATE", "COUNTRY", "REGION"

    @Column(name = "report_month", nullable = false)
    private String month; // e.g. "2026-07"

    @Column(name = "score")
    private Double score;

    @Column(name = "participation_rate")
    private Double participationRate;

    @Column(name = "total_responses")
    private Integer totalResponses;
}

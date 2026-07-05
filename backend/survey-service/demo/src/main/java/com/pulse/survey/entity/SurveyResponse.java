package com.pulse.survey.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey_responses", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"survey_id", "employee_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
}

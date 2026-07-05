package com.pulse.survey.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version", nullable = false, unique = true)
    private Integer version;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
}

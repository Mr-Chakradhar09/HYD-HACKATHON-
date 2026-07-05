package com.pulse.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionStatusResponse {
    private Long surveyId;
    private String employeeId;
    private boolean submitted;
    private LocalDateTime submittedAt;
}

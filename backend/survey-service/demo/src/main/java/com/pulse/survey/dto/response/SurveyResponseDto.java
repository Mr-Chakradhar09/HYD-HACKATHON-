package com.pulse.survey.dto.response;

import com.pulse.survey.enums.SurveyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponseDto {
    private Long id;
    private String title;
    private String month;
    private String year;
    private String location;
    private SurveyStatus status;
    private Integer version;
    private LocalDateTime publishedDate;
    private List<QuestionResponse> questions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

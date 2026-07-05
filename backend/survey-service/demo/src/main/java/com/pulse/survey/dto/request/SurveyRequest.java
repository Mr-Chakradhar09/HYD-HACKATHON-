package com.pulse.survey.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyRequest {

    @NotBlank(message = "Survey title is required")
    private String title;

    @NotBlank(message = "Month is required")
    private String month;

    @NotNull(message = "Year is required")
    @Min(value = 2020, message = "Year must be valid")
    @Max(value = 2100, message = "Year must be valid")
    private Integer year;

    @NotBlank(message = "Location is required")
    private String location;

    private Integer version; // Optional: Specific question bank version. Defaults to latest if empty.

    private List<Long> questionIds; // List of questions selected for this survey.
}

package com.virtusa.pulse.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class QuestionGenerationRequest {

    @NotNull(message = "Source survey ID cannot be null")
    private Long sourceSurveyId;

    @NotBlank(message = "Location cannot be blank")
    private String location;

    @NotNull(message = "Target survey ID cannot be null")
    private Long targetSurveyId;

    public QuestionGenerationRequest() {
    }

    public QuestionGenerationRequest(Long sourceSurveyId, String location, Long targetSurveyId) {
        this.sourceSurveyId = sourceSurveyId;
        this.location = location;
        this.targetSurveyId = targetSurveyId;
    }

    public Long getSourceSurveyId() {
        return sourceSurveyId;
    }

    public void setSourceSurveyId(Long sourceSurveyId) {
        this.sourceSurveyId = sourceSurveyId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getTargetSurveyId() {
        return targetSurveyId;
    }

    public void setTargetSurveyId(Long targetSurveyId) {
        this.targetSurveyId = targetSurveyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionGenerationRequest that = (QuestionGenerationRequest) o;
        return Objects.equals(sourceSurveyId, that.sourceSurveyId) &&
                Objects.equals(location, that.location) &&
                Objects.equals(targetSurveyId, that.targetSurveyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceSurveyId, location, targetSurveyId);
    }

    @Override
    public String toString() {
        return "QuestionGenerationRequest{" +
                "sourceSurveyId=" + sourceSurveyId +
                ", location='" + location + '\'' +
                ", targetSurveyId=" + targetSurveyId +
                '}';
    }
}

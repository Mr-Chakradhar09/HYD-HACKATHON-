package com.virtusa.pulse.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class QuestionDraftDto {

    @NotBlank(message = "Question text cannot be blank")
    private String text;

    @NotBlank(message = "Category cannot be blank")
    private String category;

    @NotBlank(message = "Location cannot be blank")
    private String location;

    @NotNull(message = "Survey ID cannot be null")
    private Long surveyId;

    public QuestionDraftDto() {
    }

    public QuestionDraftDto(String text, String category, String location, Long surveyId) {
        this.text = text;
        this.category = category;
        this.location = location;
        this.surveyId = surveyId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionDraftDto that = (QuestionDraftDto) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(category, that.category) &&
                Objects.equals(location, that.location) &&
                Objects.equals(surveyId, that.surveyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, category, location, surveyId);
    }

    @Override
    public String toString() {
        return "QuestionDraftDto{" +
                "text='" + text + '\'' +
                ", category='" + category + '\'' +
                ", location='" + location + '\'' +
                ", surveyId=" + surveyId +
                '}';
    }
}

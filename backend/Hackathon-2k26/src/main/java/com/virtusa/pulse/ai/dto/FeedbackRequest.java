package com.virtusa.pulse.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public class FeedbackRequest {

    @NotNull(message = "Survey ID cannot be null")
    private Long surveyId;

    @NotBlank(message = "Location cannot be blank")
    private String location;

    @NotEmpty(message = "Feedbacks list cannot be empty")
    @Valid
    private List<FeedbackItem> feedbacks;

    public FeedbackRequest() {
    }

    public FeedbackRequest(Long surveyId, String location, List<FeedbackItem> feedbacks) {
        this.surveyId = surveyId;
        this.location = location;
        this.feedbacks = feedbacks;
    }

    public Long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<FeedbackItem> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<FeedbackItem> feedbacks) {
        this.feedbacks = feedbacks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedbackRequest that = (FeedbackRequest) o;
        return Objects.equals(surveyId, that.surveyId) &&
                Objects.equals(location, that.location) &&
                Objects.equals(feedbacks, that.feedbacks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surveyId, location, feedbacks);
    }

    @Override
    public String toString() {
        return "FeedbackRequest{" +
                "surveyId=" + surveyId +
                ", location='" + location + '\'' +
                ", feedbacks=" + feedbacks +
                '}';
    }
}

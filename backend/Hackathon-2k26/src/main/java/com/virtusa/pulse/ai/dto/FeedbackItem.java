package com.virtusa.pulse.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class FeedbackItem {

    @NotNull(message = "Feedback ID cannot be null")
    private Long feedbackId;

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String comment;

    public FeedbackItem() {
    }

    public FeedbackItem(Long feedbackId, Integer rating, String comment) {
        this.feedbackId = feedbackId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedbackItem that = (FeedbackItem) o;
        return Objects.equals(feedbackId, that.feedbackId) &&
                Objects.equals(rating, that.rating) &&
                Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackId, rating, comment);
    }

    @Override
    public String toString() {
        return "FeedbackItem{" +
                "feedbackId=" + feedbackId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                '}';
    }
}

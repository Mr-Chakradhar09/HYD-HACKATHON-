package com.virtusa.pulse.ai.dto;

import java.util.Objects;

public class SentimentDetailDto {

    private Long feedbackId;
    private Integer rating;
    private String comment;
    private Double sentimentScore;
    private String sentimentLabel;
    private String detectedTheme;

    public SentimentDetailDto() {
    }

    public SentimentDetailDto(Long feedbackId, Integer rating, String comment, Double sentimentScore, String sentimentLabel, String detectedTheme) {
        this.feedbackId = feedbackId;
        this.rating = rating;
        this.comment = comment;
        this.sentimentScore = sentimentScore;
        this.sentimentLabel = sentimentLabel;
        this.detectedTheme = detectedTheme;
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

    public Double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(Double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public String getSentimentLabel() {
        return sentimentLabel;
    }

    public void setSentimentLabel(String sentimentLabel) {
        this.sentimentLabel = sentimentLabel;
    }

    public String getDetectedTheme() {
        return detectedTheme;
    }

    public void setDetectedTheme(String detectedTheme) {
        this.detectedTheme = detectedTheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SentimentDetailDto that = (SentimentDetailDto) o;
        return Objects.equals(feedbackId, that.feedbackId) &&
                Objects.equals(rating, that.rating) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(sentimentScore, that.sentimentScore) &&
                Objects.equals(sentimentLabel, that.sentimentLabel) &&
                Objects.equals(detectedTheme, that.detectedTheme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackId, rating, comment, sentimentScore, sentimentLabel, detectedTheme);
    }

    @Override
    public String toString() {
        return "SentimentDetailDto{" +
                "feedbackId=" + feedbackId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", sentimentScore=" + sentimentScore +
                ", sentimentLabel='" + sentimentLabel + '\'' +
                ", detectedTheme='" + detectedTheme + '\'' +
                '}';
    }
}

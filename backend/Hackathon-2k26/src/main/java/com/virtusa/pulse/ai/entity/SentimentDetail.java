package com.virtusa.pulse.ai.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "sentiment_details")
public class SentimentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private SentimentReport report;

    @Column(name = "feedback_id", nullable = false)
    private Long feedbackId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Lob
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "sentiment_score")
    private Double sentimentScore;

    @Column(name = "sentiment_label")
    private String sentimentLabel;

    @Column(name = "detected_theme")
    private String detectedTheme;

    public SentimentDetail() {
    }

    public SentimentDetail(Long feedbackId, Integer rating, String comment, Double sentimentScore, String sentimentLabel, String detectedTheme) {
        this.feedbackId = feedbackId;
        this.rating = rating;
        this.comment = comment;
        this.sentimentScore = sentimentScore;
        this.sentimentLabel = sentimentLabel;
        this.detectedTheme = detectedTheme;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SentimentReport getReport() {
        return report;
    }

    public void setReport(SentimentReport report) {
        this.report = report;
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
        SentimentDetail that = (SentimentDetail) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(feedbackId, that.feedbackId) &&
                Objects.equals(rating, that.rating) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(sentimentScore, that.sentimentScore) &&
                Objects.equals(sentimentLabel, that.sentimentLabel) &&
                Objects.equals(detectedTheme, that.detectedTheme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, feedbackId, rating, comment, sentimentScore, sentimentLabel, detectedTheme);
    }

    @Override
    public String toString() {
        return "SentimentDetail{" +
                "id=" + id +
                ", feedbackId=" + feedbackId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", sentimentScore=" + sentimentScore +
                ", sentimentLabel='" + sentimentLabel + '\'' +
                ", detectedTheme='" + detectedTheme + '\'' +
                '}';
    }
}

package com.virtusa.pulse.ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "sentiment_reports")
public class SentimentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "survey_id", nullable = false)
    private Long surveyId;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "average_score", nullable = false)
    private Double averageScore;

    @Column(name = "overall_sentiment", nullable = false)
    private String overallSentiment;

    @Lob
    @Column(name = "sentiment_summary", columnDefinition = "TEXT")
    private String sentimentSummary;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SentimentDetail> details = new ArrayList<>();

    public SentimentReport() {
    }

    public SentimentReport(Long surveyId, String location, Double averageScore, String overallSentiment, String sentimentSummary, LocalDateTime createdDate) {
        this.surveyId = surveyId;
        this.location = location;
        this.averageScore = averageScore;
        this.overallSentiment = overallSentiment;
        this.sentimentSummary = sentimentSummary;
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public String getOverallSentiment() {
        return overallSentiment;
    }

    public void setOverallSentiment(String overallSentiment) {
        this.overallSentiment = overallSentiment;
    }

    public String getSentimentSummary() {
        return sentimentSummary;
    }

    public void setSentimentSummary(String sentimentSummary) {
        this.sentimentSummary = sentimentSummary;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public List<SentimentDetail> getDetails() {
        return details;
    }

    public void setDetails(List<SentimentDetail> details) {
        this.details = details;
    }

    public void addDetail(SentimentDetail detail) {
        details.add(detail);
        detail.setReport(this);
    }

    public void removeDetail(SentimentDetail detail) {
        details.remove(detail);
        detail.setReport(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SentimentReport that = (SentimentReport) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(surveyId, that.surveyId) &&
                Objects.equals(location, that.location) &&
                Objects.equals(averageScore, that.averageScore) &&
                Objects.equals(overallSentiment, that.overallSentiment) &&
                Objects.equals(sentimentSummary, that.sentimentSummary) &&
                Objects.equals(createdDate, that.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, surveyId, location, averageScore, overallSentiment, sentimentSummary, createdDate);
    }

    @Override
    public String toString() {
        return "SentimentReport{" +
                "id=" + id +
                ", surveyId=" + surveyId +
                ", location='" + location + '\'' +
                ", averageScore=" + averageScore +
                ", overallSentiment='" + overallSentiment + '\'' +
                ", sentimentSummary='" + sentimentSummary + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}

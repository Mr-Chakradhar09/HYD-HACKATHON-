package com.virtusa.pulse.ai.dto;

import java.util.List;
import java.util.Objects;

public class SentimentReportResponseDto {

    private Long reportId;
    private Long surveyId;
    private String location;
    private Double averageScore;
    private String overallSentiment;
    private String sentimentSummary;
    private List<SentimentDetailDto> details;

    public SentimentReportResponseDto() {
    }

    public SentimentReportResponseDto(Long reportId, Long surveyId, String location, Double averageScore, String overallSentiment, String sentimentSummary, List<SentimentDetailDto> details) {
        this.reportId = reportId;
        this.surveyId = surveyId;
        this.location = location;
        this.averageScore = averageScore;
        this.overallSentiment = overallSentiment;
        this.sentimentSummary = sentimentSummary;
        this.details = details;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
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

    public List<SentimentDetailDto> getDetails() {
        return details;
    }

    public void setDetails(List<SentimentDetailDto> details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SentimentReportResponseDto that = (SentimentReportResponseDto) o;
        return Objects.equals(reportId, that.reportId) &&
                Objects.equals(surveyId, that.surveyId) &&
                Objects.equals(location, that.location) &&
                Objects.equals(averageScore, that.averageScore) &&
                Objects.equals(overallSentiment, that.overallSentiment) &&
                Objects.equals(sentimentSummary, that.sentimentSummary) &&
                Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, surveyId, location, averageScore, overallSentiment, sentimentSummary, details);
    }

    @Override
    public String toString() {
        return "SentimentReportResponseDto{" +
                "reportId=" + reportId +
                ", surveyId=" + surveyId +
                ", location='" + location + '\'' +
                ", averageScore=" + averageScore +
                ", overallSentiment='" + overallSentiment + '\'' +
                ", sentimentSummary='" + sentimentSummary + '\'' +
                ", details=" + details +
                '}';
    }
}

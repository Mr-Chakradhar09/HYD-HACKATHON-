package com.pulse.survey.dto.response;

import com.pulse.survey.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyReportResponse {
    private Long surveyId;
    private String title;
    private String month;
    private String year;
    private String location;
    private Long totalResponses;
    private Map<String, Double> categoryAverages;
    private List<QuestionMetrics> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionMetrics {
        private Long questionId;
        private String questionText;
        private String category;
        private QuestionType questionType;
        private Double averageRating;
        private Map<Integer, Long> ratingDistribution; // count of 1s, 2s, 3s, 4s, 5s
        private List<String> comments;
    }
}

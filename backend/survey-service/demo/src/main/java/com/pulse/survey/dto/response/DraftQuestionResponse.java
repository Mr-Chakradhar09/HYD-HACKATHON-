package com.pulse.survey.dto.response;

import com.pulse.survey.enums.DraftQuestionStatus;
import com.pulse.survey.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftQuestionResponse {
    private Long id;
    private String questionText;
    private String category;
    private QuestionType questionType;
    private String reason;
    private Double confidence;
    private DraftQuestionStatus status;
    private LocalDateTime generatedDate;
    private LocalDateTime approvedDate;
}

package com.pulse.survey.dto.request;

import com.pulse.survey.enums.DraftQuestionStatus;
import com.pulse.survey.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftQuestionRequest {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    private String reason;

    private Double confidence;

    private DraftQuestionStatus status;
}

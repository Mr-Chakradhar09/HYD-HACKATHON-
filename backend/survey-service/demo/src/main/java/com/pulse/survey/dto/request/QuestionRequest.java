package com.pulse.survey.dto.request;

import com.pulse.survey.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question text cannot exceed 1000 characters")
    private String questionText;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;
}

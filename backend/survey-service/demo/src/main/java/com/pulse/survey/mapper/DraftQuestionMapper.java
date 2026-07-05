package com.pulse.survey.mapper;

import com.pulse.survey.dto.request.DraftQuestionRequest;
import com.pulse.survey.dto.response.DraftQuestionResponse;
import com.pulse.survey.entity.DraftQuestion;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DraftQuestionMapper {

    DraftQuestionResponse toResponse(DraftQuestion draftQuestion);

    List<DraftQuestionResponse> toResponseList(List<DraftQuestion> draftQuestions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "generatedDate", ignore = true)
    @Mapping(target = "approvedDate", ignore = true)
    DraftQuestion toEntity(DraftQuestionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "generatedDate", ignore = true)
    @Mapping(target = "approvedDate", ignore = true)
    void updateEntityFromRequest(DraftQuestionRequest request, @MappingTarget DraftQuestion entity);
}

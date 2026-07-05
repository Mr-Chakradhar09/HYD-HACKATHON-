package com.pulse.survey.mapper;

import com.pulse.survey.dto.request.SurveyRequest;
import com.pulse.survey.dto.response.SurveyResponseDto;
import com.pulse.survey.entity.Survey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SurveyMapper {

    @Mapping(target = "questions", ignore = true)
    SurveyResponseDto toResponseDto(Survey survey);

    List<SurveyResponseDto> toResponseDtoList(List<Survey> surveys);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "publishedDate", ignore = true)
    Survey toEntity(SurveyRequest request);
}

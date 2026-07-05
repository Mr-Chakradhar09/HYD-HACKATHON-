package com.pulse.survey.mapper;

import com.pulse.survey.dto.request.SurveyRequest;
import com.pulse.survey.dto.response.SurveyResponseDto;
import com.pulse.survey.entity.Survey;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-04T16:14:37+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.19 (Microsoft)"
)
@Component
public class SurveyMapperImpl implements SurveyMapper {

    @Override
    public SurveyResponseDto toResponseDto(Survey survey) {
        if ( survey == null ) {
            return null;
        }

        SurveyResponseDto.SurveyResponseDtoBuilder surveyResponseDto = SurveyResponseDto.builder();

        surveyResponseDto.id( survey.getId() );
        surveyResponseDto.title( survey.getTitle() );
        surveyResponseDto.month( survey.getMonth() );
        if ( survey.getYear() != null ) {
            surveyResponseDto.year( String.valueOf( survey.getYear() ) );
        }
        surveyResponseDto.location( survey.getLocation() );
        surveyResponseDto.status( survey.getStatus() );
        surveyResponseDto.version( survey.getVersion() );
        surveyResponseDto.publishedDate( survey.getPublishedDate() );
        surveyResponseDto.createdAt( survey.getCreatedAt() );
        surveyResponseDto.updatedAt( survey.getUpdatedAt() );

        return surveyResponseDto.build();
    }

    @Override
    public List<SurveyResponseDto> toResponseDtoList(List<Survey> surveys) {
        if ( surveys == null ) {
            return null;
        }

        List<SurveyResponseDto> list = new ArrayList<SurveyResponseDto>( surveys.size() );
        for ( Survey survey : surveys ) {
            list.add( toResponseDto( survey ) );
        }

        return list;
    }

    @Override
    public Survey toEntity(SurveyRequest request) {
        if ( request == null ) {
            return null;
        }

        Survey.SurveyBuilder survey = Survey.builder();

        survey.title( request.getTitle() );
        survey.month( request.getMonth() );
        survey.year( request.getYear() );
        survey.location( request.getLocation() );
        survey.version( request.getVersion() );

        return survey.build();
    }
}

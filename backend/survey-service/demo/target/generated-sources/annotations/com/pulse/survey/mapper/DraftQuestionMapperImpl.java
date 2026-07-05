package com.pulse.survey.mapper;

import com.pulse.survey.dto.request.DraftQuestionRequest;
import com.pulse.survey.dto.response.DraftQuestionResponse;
import com.pulse.survey.entity.DraftQuestion;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-04T16:14:39+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.19 (Microsoft)"
)
@Component
public class DraftQuestionMapperImpl implements DraftQuestionMapper {

    @Override
    public DraftQuestionResponse toResponse(DraftQuestion draftQuestion) {
        if ( draftQuestion == null ) {
            return null;
        }

        DraftQuestionResponse.DraftQuestionResponseBuilder draftQuestionResponse = DraftQuestionResponse.builder();

        draftQuestionResponse.id( draftQuestion.getId() );
        draftQuestionResponse.questionText( draftQuestion.getQuestionText() );
        draftQuestionResponse.category( draftQuestion.getCategory() );
        draftQuestionResponse.questionType( draftQuestion.getQuestionType() );
        draftQuestionResponse.reason( draftQuestion.getReason() );
        draftQuestionResponse.confidence( draftQuestion.getConfidence() );
        draftQuestionResponse.status( draftQuestion.getStatus() );
        draftQuestionResponse.generatedDate( draftQuestion.getGeneratedDate() );
        draftQuestionResponse.approvedDate( draftQuestion.getApprovedDate() );

        return draftQuestionResponse.build();
    }

    @Override
    public List<DraftQuestionResponse> toResponseList(List<DraftQuestion> draftQuestions) {
        if ( draftQuestions == null ) {
            return null;
        }

        List<DraftQuestionResponse> list = new ArrayList<DraftQuestionResponse>( draftQuestions.size() );
        for ( DraftQuestion draftQuestion : draftQuestions ) {
            list.add( toResponse( draftQuestion ) );
        }

        return list;
    }

    @Override
    public DraftQuestion toEntity(DraftQuestionRequest request) {
        if ( request == null ) {
            return null;
        }

        DraftQuestion.DraftQuestionBuilder draftQuestion = DraftQuestion.builder();

        draftQuestion.questionText( request.getQuestionText() );
        draftQuestion.category( request.getCategory() );
        draftQuestion.questionType( request.getQuestionType() );
        draftQuestion.reason( request.getReason() );
        draftQuestion.confidence( request.getConfidence() );
        draftQuestion.status( request.getStatus() );

        return draftQuestion.build();
    }

    @Override
    public void updateEntityFromRequest(DraftQuestionRequest request, DraftQuestion entity) {
        if ( request == null ) {
            return;
        }

        entity.setQuestionText( request.getQuestionText() );
        entity.setCategory( request.getCategory() );
        entity.setQuestionType( request.getQuestionType() );
        entity.setReason( request.getReason() );
        entity.setConfidence( request.getConfidence() );
        entity.setStatus( request.getStatus() );
    }
}

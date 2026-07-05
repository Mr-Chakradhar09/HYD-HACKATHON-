package com.pulse.survey.mapper;

import com.pulse.survey.dto.request.QuestionRequest;
import com.pulse.survey.dto.response.QuestionResponse;
import com.pulse.survey.entity.Question;
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
public class QuestionMapperImpl implements QuestionMapper {

    @Override
    public QuestionResponse toResponse(Question question) {
        if ( question == null ) {
            return null;
        }

        QuestionResponse.QuestionResponseBuilder questionResponse = QuestionResponse.builder();

        questionResponse.id( question.getId() );
        questionResponse.questionText( question.getQuestionText() );
        questionResponse.category( question.getCategory() );
        questionResponse.questionType( question.getQuestionType() );
        questionResponse.source( question.getSource() );
        questionResponse.status( question.getStatus() );
        questionResponse.version( question.getVersion() );
        questionResponse.createdBy( question.getCreatedBy() );
        questionResponse.approvedBy( question.getApprovedBy() );
        questionResponse.approvedDate( question.getApprovedDate() );
        questionResponse.createdAt( question.getCreatedAt() );
        questionResponse.updatedAt( question.getUpdatedAt() );

        return questionResponse.build();
    }

    @Override
    public List<QuestionResponse> toResponseList(List<Question> questions) {
        if ( questions == null ) {
            return null;
        }

        List<QuestionResponse> list = new ArrayList<QuestionResponse>( questions.size() );
        for ( Question question : questions ) {
            list.add( toResponse( question ) );
        }

        return list;
    }

    @Override
    public Question toEntity(QuestionRequest request) {
        if ( request == null ) {
            return null;
        }

        Question.QuestionBuilder question = Question.builder();

        question.questionText( request.getQuestionText() );
        question.category( request.getCategory() );
        question.questionType( request.getQuestionType() );

        return question.build();
    }
}

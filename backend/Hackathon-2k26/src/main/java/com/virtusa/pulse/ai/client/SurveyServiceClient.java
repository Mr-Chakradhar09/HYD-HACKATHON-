package com.virtusa.pulse.ai.client;

import com.virtusa.pulse.ai.dto.QuestionDraftDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "survey-service", url = "${feign.survey-service.url:http://localhost:8082}")
public interface SurveyServiceClient {

    @PostMapping("/api/surveys/drafts")
    ResponseEntity<List<QuestionDraftDto>> saveDraftQuestions(@RequestBody List<QuestionDraftDto> drafts);
}

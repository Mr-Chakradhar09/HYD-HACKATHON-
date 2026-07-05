package com.pulse.survey.service.impl;

import com.pulse.survey.dto.request.DraftQuestionRequest;
import com.pulse.survey.dto.response.DraftQuestionResponse;
import com.pulse.survey.entity.DraftQuestion;
import com.pulse.survey.enums.DraftQuestionStatus;
import com.pulse.survey.exception.ResourceNotFoundException;
import com.pulse.survey.mapper.DraftQuestionMapper;
import com.pulse.survey.repository.DraftQuestionRepository;
import com.pulse.survey.service.DraftQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftQuestionServiceImpl implements DraftQuestionService {

    private final DraftQuestionRepository draftQuestionRepository;
    private final DraftQuestionMapper draftQuestionMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DraftQuestionResponse> getDraftQuestions(Pageable pageable, DraftQuestionStatus status) {
        log.info("Fetching draft questions with status filter: {}", status);
        Page<DraftQuestion> drafts;
        if (status != null) {
            drafts = draftQuestionRepository.findByStatus(status, pageable);
        } else {
            drafts = draftQuestionRepository.findAll(pageable);
        }
        return drafts.map(draftQuestionMapper::toResponse);
    }

    @Override
    @Transactional
    public DraftQuestionResponse editDraftQuestion(Long id, DraftQuestionRequest request) {
        log.info("Editing draft question ID: {}", id);
        DraftQuestion draft = draftQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Draft question not found with ID: " + id));

        draftQuestionMapper.updateEntityFromRequest(request, draft);
        DraftQuestion updated = draftQuestionRepository.save(draft);
        log.info("Successfully edited draft question ID: {}", updated.getId());
        return draftQuestionMapper.toResponse(updated);
    }
}

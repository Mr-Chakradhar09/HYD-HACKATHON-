package com.pulse.survey.service.impl;

import com.pulse.survey.dto.response.QuestionResponse;
import com.pulse.survey.entity.DraftQuestion;
import com.pulse.survey.entity.Question;
import com.pulse.survey.entity.QuestionVersion;
import com.pulse.survey.enums.DraftQuestionStatus;
import com.pulse.survey.enums.QuestionStatus;
import com.pulse.survey.enums.QuestionSource;
import com.pulse.survey.exception.BadRequestException;
import com.pulse.survey.exception.ResourceNotFoundException;
import com.pulse.survey.mapper.QuestionMapper;
import com.pulse.survey.repository.DraftQuestionRepository;
import com.pulse.survey.repository.QuestionRepository;
import com.pulse.survey.repository.QuestionVersionRepository;
import com.pulse.survey.security.SecurityUtils;
import com.pulse.survey.service.HRApprovalService;
import com.pulse.survey.service.QuestionVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class HRApprovalServiceImpl implements HRApprovalService {

    private final DraftQuestionRepository draftQuestionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionVersionRepository versionRepository;
    private final QuestionVersionService versionService;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional
    public QuestionResponse approveDraftQuestion(Long id) {
        log.info("Approving draft question ID: {}", id);
        DraftQuestion draft = draftQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Draft question not found with ID: " + id));

        if (draft.getStatus() != DraftQuestionStatus.PENDING) {
            throw new BadRequestException("Only PENDING draft questions can be approved. Current status: " + draft.getStatus());
        }

        // Mark draft as approved
        draft.setStatus(DraftQuestionStatus.APPROVED);
        draft.setApprovedDate(LocalDateTime.now());
        draftQuestionRepository.save(draft);

        // Determine target version for the question bank
        Integer currentVersion = versionService.getLatestVersion();
        Integer targetVersion = currentVersion + 1;

        // Check if the target version has already been registered in this approval cycle
        boolean versionExists = versionRepository.findFirstByOrderByVersionDesc()
                .map(qv -> qv.getVersion().equals(targetVersion))
                .orElse(false);

        if (!versionExists) {
            log.info("Creating new question bank version: {} for this approval cycle.", targetVersion);
            QuestionVersion nextVer = QuestionVersion.builder()
                    .version(targetVersion)
                    .description("Version " + targetVersion + " Question Bank, including AI approved additions.")
                    .createdDate(LocalDateTime.now())
                    .build();
            versionRepository.save(nextVer);
        }

        // Add to main Question Bank
        Question question = Question.builder()
                .questionText(draft.getQuestionText())
                .category(draft.getCategory())
                .questionType(draft.getQuestionType())
                .source(QuestionSource.AI)
                .status(QuestionStatus.ACTIVE)
                .version(targetVersion)
                .createdBy("AI_SERVICE")
                .approvedBy(SecurityUtils.getCurrentUsername())
                .approvedDate(LocalDateTime.now())
                .build();

        Question saved = questionRepository.save(question);
        log.info("Successfully added approved AI question to Bank under ID: {} and Version: {}", saved.getId(), targetVersion);

        return questionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void rejectDraftQuestion(Long id) {
        log.info("Rejecting draft question ID: {}", id);
        DraftQuestion draft = draftQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Draft question not found with ID: " + id));

        if (draft.getStatus() != DraftQuestionStatus.PENDING) {
            throw new BadRequestException("Only PENDING draft questions can be rejected. Current status: " + draft.getStatus());
        }

        draft.setStatus(DraftQuestionStatus.REJECTED);
        draftQuestionRepository.save(draft);
        log.info("Draft question ID: {} has been marked as REJECTED.", id);
    }
}

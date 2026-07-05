package com.pulse.survey.service.impl;

import com.pulse.survey.dto.request.QuestionRequest;
import com.pulse.survey.dto.response.QuestionResponse;
import com.pulse.survey.entity.Question;
import com.pulse.survey.enums.QuestionStatus;
import com.pulse.survey.enums.QuestionSource;
import com.pulse.survey.exception.ResourceNotFoundException;
import com.pulse.survey.mapper.QuestionMapper;
import com.pulse.survey.repository.QuestionRepository;
import com.pulse.survey.security.SecurityUtils;
import com.pulse.survey.service.QuestionBankService;
import com.pulse.survey.service.QuestionVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionBankServiceImpl implements QuestionBankService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final QuestionVersionService versionService;

    @Override
    @Transactional
    public QuestionResponse createQuestion(QuestionRequest request) {
        log.info("Creating a new question: {}", request.getQuestionText());
        Question question = questionMapper.toEntity(request);
        
        question.setSource(QuestionSource.STATIC);
        question.setStatus(QuestionStatus.ACTIVE);
        question.setVersion(versionService.getLatestVersion());
        question.setCreatedBy(SecurityUtils.getCurrentUsername());

        Question saved = questionRepository.save(question);
        log.info("Successfully created question ID: {}", saved.getId());
        return questionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long id) {
        return questionRepository.findById(id)
                .map(questionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionResponse> getAllQuestions(Pageable pageable) {
        return questionRepository.findByStatus(QuestionStatus.ACTIVE, pageable)
                .map(questionMapper::toResponse);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        log.info("Updating question ID: {}", id);
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));

        question.setQuestionText(request.getQuestionText());
        question.setCategory(request.getCategory());
        question.setQuestionType(request.getQuestionType());

        Question updated = questionRepository.save(question);
        log.info("Successfully updated question ID: {}", updated.getId());
        return questionMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        log.info("Deactivating (soft deleting) question ID: {}", id);
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));

        question.setStatus(QuestionStatus.INACTIVE);
        questionRepository.save(question);
        log.info("Question ID: {} has been marked as INACTIVE.", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByVersion(Integer version) {
        Integer targetVersion = (version != null) ? version : versionService.getLatestVersion();
        log.info("Fetching active questions for question bank version: {}", targetVersion);
        List<Question> questions = questionRepository.findByVersionAndStatus(targetVersion, QuestionStatus.ACTIVE);
        return questionMapper.toResponseList(questions);
    }
}

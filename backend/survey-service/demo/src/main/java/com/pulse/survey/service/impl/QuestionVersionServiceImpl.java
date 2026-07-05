package com.pulse.survey.service.impl;

import com.pulse.survey.entity.QuestionVersion;
import com.pulse.survey.repository.QuestionVersionRepository;
import com.pulse.survey.service.QuestionVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionVersionServiceImpl implements QuestionVersionService {

    private final QuestionVersionRepository versionRepository;

    @Override
    public Integer getLatestVersion() {
        return versionRepository.findFirstByOrderByVersionDesc()
                .map(QuestionVersion::getVersion)
                .orElse(1); // Defaults to version 1 if no versions exist
    }

    @Override
    @Transactional
    public QuestionVersion createNewVersion(String description) {
        Integer nextVersion = getLatestVersion() + 1;
        log.info("Creating new question bank version: {} - {}", nextVersion, description);
        
        QuestionVersion questionVersion = QuestionVersion.builder()
                .version(nextVersion)
                .description(description)
                .createdDate(LocalDateTime.now())
                .build();
        
        return versionRepository.save(questionVersion);
    }
}

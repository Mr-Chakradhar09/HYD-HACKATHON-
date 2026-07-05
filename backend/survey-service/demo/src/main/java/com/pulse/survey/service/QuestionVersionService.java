package com.pulse.survey.service;

import com.pulse.survey.entity.QuestionVersion;

public interface QuestionVersionService {
    Integer getLatestVersion();
    QuestionVersion createNewVersion(String description);
}

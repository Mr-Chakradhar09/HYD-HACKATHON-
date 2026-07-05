package com.pulse.survey.repository;

import com.pulse.survey.entity.QuestionVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionVersionRepository extends JpaRepository<QuestionVersion, Long> {

    Optional<QuestionVersion> findFirstByOrderByVersionDesc();
}

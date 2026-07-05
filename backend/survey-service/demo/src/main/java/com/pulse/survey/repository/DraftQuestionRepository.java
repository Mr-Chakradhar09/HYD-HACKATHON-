package com.pulse.survey.repository;

import com.pulse.survey.entity.DraftQuestion;
import com.pulse.survey.enums.DraftQuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DraftQuestionRepository extends JpaRepository<DraftQuestion, Long> {

    Page<DraftQuestion> findByStatus(DraftQuestionStatus status, Pageable pageable);

    List<DraftQuestion> findByStatus(DraftQuestionStatus status);
}

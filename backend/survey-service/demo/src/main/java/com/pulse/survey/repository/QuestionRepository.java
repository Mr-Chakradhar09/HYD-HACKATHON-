package com.pulse.survey.repository;

import com.pulse.survey.entity.Question;
import com.pulse.survey.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByVersionAndStatus(Integer version, QuestionStatus status);

    Page<Question> findByStatus(QuestionStatus status, Pageable pageable);

    @Query("SELECT MAX(q.version) FROM Question q")
    Optional<Integer> findMaxVersion();

    List<Question> findByVersion(Integer version);
}

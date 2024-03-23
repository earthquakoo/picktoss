package com.picktoss.picktossserver.domain.question.repository;

import com.picktoss.picktossserver.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}

package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizSetQuizRepository extends JpaRepository<QuizSetQuiz, Long> {

    @Query("SELECT qsq FROM QuizSetQuiz qsq WHERE qsq.quiz.id = :quizId")
    Optional<QuizSetQuiz> findByQuizId(@Param("quizId") Long quizId);
}

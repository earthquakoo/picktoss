package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizSetQuizRepository extends JpaRepository<QuizSetQuiz, Long> {

    @Query("SELECT qsq.quiz FROM QuizSetQuiz qsq WHERE qsq.quizSet.id = :quizSetId")
    List<Quiz> findAllQuizzesByQuizSetId(@Param("quizSetId") String quizSetId);

    @Query("SELECT qsq.quiz FROM QuizSetQuiz qsq JOIN qsq.quizSet qs WHERE qs.member.id = :memberId")
    List<Quiz> findAllQuizzesByMemberId(@Param("memberId") Long memberId);
}

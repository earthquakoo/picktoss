package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizSetQuizRepository extends JpaRepository<QuizSetQuiz, Long> {

    @Query("SELECT qsq.quiz FROM QuizSetQuiz qsq " +
            "JOIN qsq.quizSet qs " +
            "WHERE qs.id = :quizSetId " +
            "AND qs.member.id = :memberId")
    List<Quiz> findAllQuizzesByQuizSetIdAndMemberId(
            @Param("quizSetId") String quizSetId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN qsq.quizSet qs " +
            "JOIN qsq.quiz q " +
            "JOIN q.document d " +
            "JOIN d.category c " +
            "WHERE qs.member.id = :memberId " +
            "AND c.id = :categoryId " +
            "AND qs.solved = true " +
            "ORDER BY qsq.updatedAt ASC")
    List<QuizSetQuiz> findAllByMemberIdAndCategoryId(
            @Param("memberId") Long memberId,
            @Param("categoryId") Long categoryId
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN qsq.quizSet qs " +
            "WHERE qs.id = :quizSetId " +
            "AND qs.member.id = :memberId")
    List<QuizSetQuiz> findAllByQuizSetIdAndMemberId(
            @Param("quizSetId") String quizSetId,
            @Param("memberId") Long memberId
    );
}

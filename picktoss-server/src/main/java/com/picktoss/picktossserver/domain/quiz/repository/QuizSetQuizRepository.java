package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizSetQuizRepository extends JpaRepository<QuizSetQuiz, Long> {

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.category c " +
            "JOIN FETCH qsq.quizSet qs " +
            "WHERE qs.id = :quizSetId " +
            "AND qs.member.id = :memberId")
    List<QuizSetQuiz> findAllQuizzesByQuizSetIdAndMemberId(
            @Param("quizSetId") String quizSetId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quizSet qs " +
            "JOIN FETCH qsq.quiz q " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.category c " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.solved = true")
    List<QuizSetQuiz> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quizSet qs " +
            "JOIN FETCH qsq.quiz q " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.category c " +
            "WHERE qs.member.id = :memberId " +
            "AND c.id = :categoryId " +
            "AND qs.solved = true " +
            "ORDER BY qsq.updatedAt ASC")
    List<QuizSetQuiz> findAllByMemberIdAndCategoryId(
            @Param("memberId") Long memberId,
            @Param("categoryId") Long categoryId
    );

    @Query("SELECT DISTINCT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH qsq.quizSet qs " +
            "WHERE qs.id = :quizSetId " +
            "AND qs.member.id = :memberId")
    List<QuizSetQuiz> findAllByQuizSetIdAndMemberId(
            @Param("quizSetId") String quizSetId,
            @Param("memberId") Long memberId
    );
}

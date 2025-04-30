package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizSetRepository extends JpaRepository<QuizSet, String> {

    @Query("SELECT qs FROM QuizSet qs " +
            "JOIN FETCH qs.quizSetQuizzes qsq " +
            "JOIN FETCH qsq.quiz q " +
            "JOIN FETCH q.document d " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.solved = true " +
            "ORDER BY qs.createdAt DESC")
    List<QuizSet> findAllByMemberIdAndSolvedTrueOrderByCreatedAtDesc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "JOIN FETCH qs.quizSetQuizzes qsq " +
            "JOIN FETCH qsq.quiz q " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.solved = true " +
            "ORDER BY qs.createdAt DESC")
    List<QuizSet> findAllByMemberIdAndSolvedTrueAndTodayQuizSetOrderByCreatedAtDesc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "JOIN FETCH qs.quizSetQuizzes qsq " +
            "JOIN FETCH qsq.quiz q " +
            "WHERE qs.member.id = :memberId " +
            "ORDER BY qs.createdAt DESC")
    List<QuizSet> findAllByMemberIdAndTodayQuizSetOrderByCreatedAtDesc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "JOIN FETCH qs.quizSetQuizzes qsq " +
            "JOIN FETCH qsq.quiz q " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.solved = true " +
            "AND qs.createdAt >= :startDateTime " +
            "AND qs.createdAt <= :endDateTime")
    List<QuizSet> findAllByMemberIdAndSolvedTrueAndDateTime(
            @Param("memberId") Long memberId,
            @Param("startDateTime") LocalDateTime starDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT DISTINCT qs FROM QuizSet qs " +
            "JOIN FETCH qs.quizSetQuizzes qsq " +
            "JOIN FETCH qsq.quiz q " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.id = :quizSetId")
    Optional<QuizSet> findQuizSetByMemberIdAndQuizSetId(
            @Param("memberId") Long memberId,
            @Param("quizSetId") Long quizSetId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "WHERE qs.member.id = :memberId " +
            "ORDER BY qs.createdAt DESC")
    List<QuizSet> findByMemberIdAndTodayQuizSetOrderByCreatedAtDesc(
            @Param("memberId") Long memberId
    );
}

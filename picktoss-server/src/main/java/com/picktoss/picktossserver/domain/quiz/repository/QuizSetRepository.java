package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizSetRepository extends JpaRepository<QuizSet, String> {

    @Query("SELECT qs FROM QuizSet qs " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.solved = true " +
            "AND qs.quizSetType = :quizSetType " +
            "ORDER BY qs.createdAt DESC")
    List<QuizSet> findAllByMemberIdAndSolvedTrueAndQuizSetTypeOrderByCreatedAtDesc(
            @Param("memberId") Long memberId,
            @Param("quizSetType") QuizSetType quizSetType
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "JOIN FETCH qs.quizSetQuizzes qsq " +
            "JOIN FETCH qsq.quiz q " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.solved = true " +
            "AND qs.quizSetType = :quizSetType")
    List<QuizSet> findAllByMemberIdAndSolvedTrueAndQuizSetType(
            @Param("memberId") Long memberId,
            @Param("quizSetType") QuizSetType quizSetType
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "JOIN FETCH qs.quizSetQuizzes qsq " +
            "JOIN FETCH qsq.quiz q " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.solved = true")
    List<QuizSet> findAllByMemberIdAndSolvedTrue(
            @Param("memberId") Long memberId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "JOIN FETCH qs.quizSetQuizzes qsq " +
            "JOIN FETCH qsq.quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.id = :quizSetId")
    Optional<QuizSet> findQuizSetByMemberIdAndQuizSetId(
            @Param("memberId") Long memberId,
            @Param("quizSetId") String quizSetId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.quizSetType = :quizSetType " +
            "ORDER BY qs.createdAt DESC")
    List<QuizSet> findByMemberIdAndTodayQuizSetOrderByCreatedAtDesc(
            @Param("memberId") Long memberId,
            @Param("quizSetType") QuizSetType quizSetType
    );
}

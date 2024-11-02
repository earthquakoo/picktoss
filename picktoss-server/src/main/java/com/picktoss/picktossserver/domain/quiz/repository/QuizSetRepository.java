package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizSetRepository extends JpaRepository<QuizSet, String> {

    @Query("SELECT qs FROM QuizSet qs " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.isTodayQuizSet = true " +
            "AND qs.solved = true " +
            "ORDER BY qs.createdAt DESC")
    List<QuizSet> findAllByMemberIdAndIsTodayQuizSetTrueAndSolvedTrueOrderByCreatedAtDesc(
            @Param("memberId") Long memberId
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
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.category c " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.id = :quizSetId")
    Optional<QuizSet> findQuizSetByMemberIdAndQuizSetId(
            @Param("memberId") Long memberId,
            @Param("quizSetId") String quizSetId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "WHERE qs.id = :quizSetId " +
            "AND qs.member.id = :memberId")
    Optional<QuizSet> findByQuizSetIdAndMemberId(
            @Param("quizSetId") String quizSetId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.isTodayQuizSet = true " +
            "ORDER BY qs.createdAt DESC")
    List<QuizSet> findByMemberIdAndTodayQuizSetIsOrderByCreatedAtDesc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.isTodayQuizSet = true " +
            "ORDER BY qs.createdAt ASC")
    List<QuizSet> findByMemberIdAndTodayQuizSetIsOrderByCreatedAtAsc(
            @Param("memberId") Long memberId
    );
}

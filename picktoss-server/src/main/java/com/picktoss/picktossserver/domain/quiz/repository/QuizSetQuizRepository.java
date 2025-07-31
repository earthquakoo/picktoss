package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizSetQuizRepository extends JpaRepository<QuizSetQuiz, Long> {

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "JOIN FETCH qsq.quizSet qs " +
            "WHERE qs.id = :quizSetId " +
            "AND qs.member.id = :memberId")
    List<QuizSetQuiz> findAllByQuizSetIdAndMemberId(
            @Param("quizSetId") Long quizSetId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quizSet qs " +
            "JOIN FETCH qsq.quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE qs.id = :quizSetId " +
            "AND qs.member.id = :memberId " +
            "AND qs.solved = true")
    List<QuizSetQuiz> findAllByQuizSetIdAndMemberIdAndSolvedTrue(
            @Param("quizSetId") Long quizSetId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quizSet qs " +
            "JOIN FETCH qsq.quiz q " +
            "JOIN FETCH q.document d " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.solved = true " +
            "AND qsq.createdAt >= :startDateTime " +
            "AND qsq.createdAt <= :endDateTime")
    List<QuizSetQuiz> findAllByMemberIdAndSolvedTrueAndDateTime(
            @Param("memberId") Long memberId,
            @Param("startDateTime") LocalDateTime starDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quizSet qs " +
            "JOIN FETCH qsq.quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE qs.member.id = :memberId " +
            "AND d.id = :documentId " +
            "AND qsq.createdAt >= :oneMonthAgo " +
            "AND qs.solved = true")
    List<QuizSetQuiz> findAllByMemberIdAndDocumentIdAndCreatedAtAfterAndSolvedTrue(
            @Param("memberId") Long memberId,
            @Param("documentId") Long documentId,
            @Param("oneMonthAgo") LocalDateTime oneMonthAgo
    );
}

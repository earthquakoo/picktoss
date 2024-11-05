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
            "JOIN FETCH d.directory di " +
            "JOIN FETCH qsq.quizSet qs " +
            "WHERE qs.id = :quizSetId " +
            "AND qs.member.id = :memberId")
    List<QuizSetQuiz> findAllByQuizSetIdAndMemberId(
            @Param("quizSetId") String quizSetId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quizSet qs " +
            "JOIN FETCH qsq.quiz q " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory di " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.solved = true")
    List<QuizSetQuiz> findAllByMemberIdAndSolvedTrue(
            @Param("memberId") Long memberId
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quizSet qs " +
            "JOIN FETCH qsq.quiz q " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory di " +
            "WHERE qs.member.id = :memberId " +
            "AND di.id = :directoryId " +
            "AND qs.solved = true")
    List<QuizSetQuiz> findAllByMemberIdAndDirectoryIdAndSolvedTrue(
            @Param("memberId") Long memberId,
            @Param("directoryId") Long directoryId
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quizSet qs " +
            "JOIN FETCH qsq.quiz q " +
            "JOIN FETCH q.document d " +
            "WHERE qs.member.id = :memberId " +
            "AND d.id = :documentId " +
            "AND qs.solved = true " +
            "AND qsq.createdAt >= :sevenDaysAgo")
    List<QuizSetQuiz> findByMemberIdAndDocumentIdAndSolvedTrueAndCreatedAtAfter(
            @Param("memberId") Long memberId,
            @Param("documentId") Long documentId,
            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo
    );

    @Query("SELECT qsq FROM QuizSetQuiz qsq " +
            "JOIN FETCH qsq.quizSet qs " +
            "JOIN FETCH qsq.quiz q " +
            "JOIN FETCH q.document d " +
            "WHERE qs.member.id = :memberId " +
            "AND qsq.createdAt >= :sevenDaysAgo")
    List<QuizSetQuiz> findAllByMemberIdAndCreatedAtAfter(
            @Param("memberId") Long memberId,
            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo
    );
}

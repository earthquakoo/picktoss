package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE d.id = :documentId " +
            "AND dir.member.id = :memberId")
    List<Quiz> findAllByDocumentIdAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE d.id = :documentId " +
            "AND q.isLatest = true " +
            "AND dir.member.id = :memberId")
    List<Quiz> findAllByDocumentIdAndIsLatestAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE d.id = :documentId " +
            "AND q.quizType = :quizType " +
            "AND dir.member.id = :memberId")
    List<Quiz> findAllByDocumentIdAndQuizTypeAndMemberId(
            @Param("documentId") Long documentId,
            @Param("quizType") QuizType quizType,
            @Param("memberId") Long memberId
    );

    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE dir.member.id = :memberId " +
            "AND dir.id = :directoryId")
    List<Quiz> findAllByMemberIdAndDirectoryId(
            @Param("memberId") Long memberId,
            @Param("directoryId") Long directoryId
    );

    @Query("SELECT q FROM Quiz q " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE q.id = :quizId " +
            "AND dir.member.id = :memberId")
    Optional<Quiz> findByQuizIdAndMemberId(
            @Param("quizId") Long quizId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN q.document d " +
            "JOIN d.directory dir " +
            "WHERE dir.member.id = :memberId " +
            "AND q.id IN :ids")
    List<Quiz> findAllByMemberIdAndQuizIds(
            @Param("memberId") Long memberId,
            @Param("ids") List<Long> quizIds
    );

    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE dir.member.id = :memberId " +
            "AND q.isReviewNeeded = true")
    List<Quiz> findAllByMemberIdAndIsReviewNeededTrue(
            @Param("memberId") Long memberId
    );

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN q.document d " +
            "JOIN d.directory dir " +
            "WHERE dir.member.id = :memberId " +
            "ORDER BY q.deliveredCount ASC")
    List<Quiz> findAllByMemberIdForTest(
            @Param("memberId") Long memberId
    );
}

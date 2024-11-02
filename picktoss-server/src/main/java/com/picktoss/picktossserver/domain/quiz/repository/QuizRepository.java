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
            "JOIN FETCH d.category c " +
            "WHERE d.id = :documentId " +
            "AND c.member.id = :memberId")
    List<Quiz> findAllByDocumentIdAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.category c " +
            "WHERE d.id = :documentId " +
            "AND q.quizType = :quizType " +
            "AND c.member.id = :memberId")
    List<Quiz> findAllByDocumentIdAndQuizTypeAndMemberId(
            @Param("documentId") Long documentId,
            @Param("quizType") QuizType quizType,
            @Param("memberId") Long memberId
    );

    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.category c " +
            "WHERE c.member.id = :memberId")
    List<Quiz> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT q FROM Quiz q " +
            "JOIN FETCH q.document d " +
            "WHERE q.quizType = :quizType " +
            "AND d.id = :documentId " +
            "ORDER BY q.deliveredCount ASC")
    List<Quiz> findByQuizTypeAndDocumentId(
            @Param("quizType") QuizType quizType,
            @Param("documentId") Long documentId
    );

    @Query("SELECT q FROM Quiz q " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.category c " +
            "WHERE q.id = :quizId " +
            "AND c.member.id = :memberId")
    Optional<Quiz> findByQuizIdAndMemberId(
            @Param("quizId") Long quizId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.category c " +
            "WHERE d.id = :documentId " +
            "AND c.member.id = :memberId")
    List<Quiz> findByDocumentIdAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN q.document d " +
            "JOIN d.category c " +
            "WHERE c.member.id = :memberId " +
            "ORDER BY q.deliveredCount ASC")
    List<Quiz> findAllByMemberIdForTest(
            @Param("memberId") Long memberId
    );


    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN q.document d " +
            "JOIN d.category c " +
            "WHERE c.member.id = :memberId " +
            "AND q.id IN :ids")
    List<Quiz> findQuizzesByQuizIds(
            @Param("memberId") Long memberId,
            @Param("ids") List<Long> quizIds
    );
}

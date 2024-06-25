package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT q FROM Quiz q " +
            "WHERE q.bookmark = true")
    List<Quiz> findByBookmark();

    @Query("SELECT q FROM Quiz q " +
            "JOIN q.document d " +
            "JOIN d.category c " +
            "WHERE d.id = :documentId " +
            "AND c.member.id = :memberId")
    List<Quiz> findAllByDocumentIdAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT q FROM Quiz q " +
            "WHERE q.document.id = :documentId " +
            "AND q.quizType = :quizType " +
            "ORDER BY q.deliveredCount ASC")
    List<Quiz> findByDocumentIdAndQuizType(
            @Param("documentId") Long documentId,
            @Param("quizType") QuizType quizType
    );

    @Query("SELECT q FROM Quiz q " +
            "WHERE q.document.id = :documentId " +
            "AND q.latest = true")
    List<Quiz> findByDocumentIdAndLatestIs(@Param("documentId") Long documentId);

    @Query("SELECT q FROM Quiz q " +
            "WHERE q.id = :quizId " +
            "AND q.document.id = :documentId")
    Optional<Quiz> findByQuizIdAndDocumentId(
            @Param("quizId") Long quizId,
            @Param("documentId") Long documentId
    );

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Query("SELECT q FROM Quiz q " +
            "JOIN FETCH q.options " +
            "JOIN q.document d " +
            "JOIN d.category c " +
            "WHERE c.member.id = :memberId " +
            "ORDER BY q.deliveredCount ASC")
    List<Quiz> findAllByMemberIdForTest(
            @Param("memberId") Long memberId
    );
}

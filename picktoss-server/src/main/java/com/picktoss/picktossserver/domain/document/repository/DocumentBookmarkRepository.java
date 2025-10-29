package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentBookmarkRepository extends JpaRepository<DocumentBookmark, Long> {

    Optional<DocumentBookmark> findByDocumentAndMember(Document document, Member member);

    @Query("SELECT db FROM DocumentBookmark db " +
            "JOIN FETCH db.document d " +
            "LEFT JOIN FETCH d.quizzes q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE db.member.id = :memberId")
    List<DocumentBookmark> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT db FROM DocumentBookmark db " +
            "JOIN FETCH db.document d " +
            "LEFT JOIN FETCH d.quizzes q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE db.member.id = :memberId " +
            "AND d.language = :language")
    List<DocumentBookmark> findAllByMemberIdAndLanguage(
            @Param("memberId") Long memberId,
            @Param("language") String language
    );

    @Query("SELECT db FROM DocumentBookmark db " +
            "JOIN FETCH db.document d " +
            "LEFT JOIN FETCH d.quizzes q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE db.member.id = :memberId " +
            "AND q.quizType = :quizType")
    List<DocumentBookmark> findAllByMemberIdAndQuizType(
            @Param("memberId") Long memberId,
            @Param("quizType") QuizType quizType
    );

    @Query("SELECT db FROM DocumentBookmark db " +
            "JOIN FETCH db.document d " +
            "LEFT JOIN FETCH d.quizzes q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE db.member.id = :memberId " +
            "AND q.quizType = :quizType " +
            "AND d.language = :language")
    List<DocumentBookmark> findAllByMemberIdAndQuizTypeAndLanguage(
            @Param("memberId") Long memberId,
            @Param("quizType") QuizType quizType,
            @Param("language") String language
    );

    @Query("SELECT db FROM DocumentBookmark db " +
            "JOIN FETCH db.document d " +
            "LEFT JOIN d.quizzes q " +
            "WHERE db.member.id = :memberId " +
            "AND d.isPublic = true " +
            "AND d.language = :language " +
            "GROUP BY db " +
            "ORDER BY COUNT(q) DESC")
    List<DocumentBookmark> findAllByMemberIdAndIsPublicTrueAndLanguageOrderByQuizCountDesc(
            @Param("memberId") Long memberId,
            @Param("language") String language
    );

    @Query("SELECT db FROM DocumentBookmark db " +
            "JOIN FETCH db.document d " +
            "LEFT JOIN d.quizzes q " +
            "WHERE db.member.id = :memberId " +
            "AND d.isPublic = true " +
            "AND d.language = :language " +
            "ORDER BY db.createdAt DESC")
    List<DocumentBookmark> findAllByMemberIdAndIsPublicTrueAndLanguageOrderByCreatedAtDesc(
            @Param("memberId") Long memberId,
            @Param("language") String language
    );

    @Query("SELECT db FROM DocumentBookmark db " +
            "JOIN FETCH db.document d " +
            "LEFT JOIN d.quizzes q " +
            "WHERE db.member.id = :memberId " +
            "AND d.isPublic = true " +
            "AND d.language = :language " +
            "ORDER BY d.name DESC")
    List<DocumentBookmark> findAllByMemberIdAndIsPublicTrueAndLanguageOrderByNameDesc(
            @Param("memberId") Long memberId,
            @Param("language") String language
    );
}

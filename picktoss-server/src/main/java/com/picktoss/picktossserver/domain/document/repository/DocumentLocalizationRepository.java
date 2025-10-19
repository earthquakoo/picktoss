package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentLocalizationRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d " +
            "JOIN FETCH d.directory dir " +
            "LEFT JOIN FETCH d.documentBookmarks " +
            "WHERE dir.member.id = :memberId " +
            "ORDER BY d.createdAt DESC")
    List<Document> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "LEFT JOIN FETCH d.quizzes " +
            "LEFT JOIN FETCH d.documentBookmarks " +
            "JOIN FETCH d.directory dir " +
            "WHERE dir.member.id = :memberId " +
            "ORDER BY d.createdAt DESC")
    List<Document> findAllByMemberIdOrderByCreatedAtDesc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "LEFT JOIN FETCH d.quizzes " +
            "LEFT JOIN FETCH d.documentBookmarks " +
            "JOIN FETCH d.directory dir " +
            "WHERE dir.member.id = :memberId " +
            "ORDER BY d.name ASC")
    List<Document> findAllByMemberIdOrderByNameAsc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "LEFT JOIN d.quizzes q " +
            "JOIN d.directory dir " +
            "WHERE dir.member.id = :memberId " +
            "GROUP BY d " +
            "ORDER BY COUNT(q) DESC")
    List<Document> findAllByMemberIdOrderByQuizCountDesc(
            @Param("memberId") Long memberId
    );

    @Query(value = """
    SELECT d.*
    FROM document d
    LEFT JOIN quiz q ON q.document_id = d.id
    LEFT JOIN document_bookmark db ON db.document_id = d.id
    JOIN directory dir ON d.directory_id = dir.id
    WHERE dir.member_id = :memberId
    GROUP BY d.id
    ORDER BY SUM(CASE WHEN q.is_review_needed = true THEN 1 ELSE 0 END) DESC
    """, nativeQuery = true)
    List<Document> findAllOrderByWrongAnswerCount(
            @Param("memberId") Long memberId
    );
}

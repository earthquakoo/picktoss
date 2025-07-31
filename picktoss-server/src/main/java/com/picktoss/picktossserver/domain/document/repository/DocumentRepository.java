package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT DISTINCT d FROM Document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE d.id = :documentId " +
            "AND dir.member.id = :memberId")
    Optional<Document> findByDocumentIdAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN FETCH d.directory dir " +
            "JOIN FETCH d.category c " +
            "LEFT JOIN FETCH d.documentBookmarks " +
            "LEFT JOIN FETCH d.quizzes q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE d.id = :documentId " +
            "AND dir.member.id = :memberId")
    Optional<Document> findDocumentWithQuizzesByDocumentIdAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN FETCH d.directory dir " +
            "JOIN FETCH d.category c " +
            "LEFT JOIN FETCH d.documentBookmarks " +
            "LEFT JOIN FETCH d.quizzes q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE d.id = :documentId")
    Optional<Document> findDocumentWithQuizzesByDocumentId(
            @Param("documentId") Long documentId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE dir.member.id = :memberId " +
            "ORDER BY d.createdAt DESC")
    List<Document> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN FETCH d.directory dir " +
            "LEFT JOIN FETCH d.quizzes " +
            "WHERE d.id IN :documentIds " +
            "AND dir.member.id = :memberId")
    List<Document> findByDocumentIdsInAndMemberId(
            @Param("documentIds") List<Long> documentIds,
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

    @Query("SELECT d FROM Document d " +
            "LEFT JOIN FETCH d.quizzes " +
            "WHERE d.isPublic = true " +
            "ORDER BY d.createdAt DESC")
    Page<Document> findAllByIsPublic(
            Pageable pageable
    );

    @Query("SELECT d FROM Document d " +
            "LEFT JOIN FETCH d.quizzes " +
            "WHERE d.isPublic = true " +
            "AND d.category.id = :categoryId " +
            "ORDER BY d.createdAt DESC")
    Page<Document> findAllByIsPublicAndCategoryId(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("SELECT d FROM Document d " +
            "LEFT JOIN FETCH d.quizzes " +
            "JOIN FETCH d.directory dir " +
            "WHERE (d.isPublic = true OR dir.member.id = :memberId) " +
            "AND d.name LIKE %:keyword%")
    List<Document> findAllByIsPublicOrOwnerAndKeyword(
            @Param("keyword") String keyword,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE d.isPublic = false " +
            "AND dir.member.id = :memberId")
    List<Document> findAllByIsNotPublicAndMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "LEFT JOIN FETCH d.documentBookmarks " +
            "LEFT JOIN FETCH d.quizzes " +
            "JOIN FETCH d.category c " +
            "JOIN FETCH d.directory dir " +
            "WHERE d.id = :documentId")
    Optional<Document> findByDocumentIdAndIsPublic(
            @Param("documentId") Long documentId
    );

}

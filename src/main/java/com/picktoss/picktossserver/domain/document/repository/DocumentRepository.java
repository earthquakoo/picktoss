package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d " +
            "JOIN d.category c " +
            "WHERE c.id = :categoryId " +
            "AND c.member.id = :memberId " +
            "ORDER BY d.createdAt DESC")
    List<Document> findAllByCategoryIdAndMemberId(
            @Param("categoryId") Long categoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.category c " +
            "WHERE d.id = :documentId " +
            "AND c.member.id = :memberId")
    Optional<Document> findByDocumentIdAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.category c " +
            "WHERE c.member.id = :memberId " +
            "ORDER BY d.createdAt DESC")
    List<Document> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT d FROM Document d " +
            "JOIN d.category c " +
            "WHERE d.id IN :documentIds " +
            "AND c.member.id = :memberId")
    List<Document> findByMemberIdAndDocumentIdIn(
            @Param("documentIds") List<Long> documentIds,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.category c " +
            "WHERE c.member.id = :memberId " +
            "ORDER BY d.id ASC")
    List<Document> findMostIncorrectDocuments(@Param("memberId") Long memberId);

    @Query("SELECT MAX(d.order) FROM Document d " +
            "JOIN d.category c " +
            "WHERE c.id = :categoryId " +
            "AND c.member.id = :memberId")
    Integer findLastOrderByCategoryIdAndMemberId(
            @Param("categoryId") Long categoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.category c " +
            "WHERE d.order >= :minDocumentOrder AND d.order < :maxDocumentOrder " +
            "AND c.member.id = :memberId " +
            "ORDER BY d.order ASC")
    List<Document> findByOrderGreaterThanEqualAndOrderLessThanOrderByOrderAsc(
            @Param("minDocumentOrder") int minDocumentOrder,
            @Param("maxDocumentOrder") int maxDocumentOrder,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.category c " +
            "WHERE d.order > :minDocumentOrder AND d.order <= :maxDocumentOrder " +
            "AND c.member.id = :memberId " +
            "ORDER BY d.order ASC")
    List<Document> findByOrderGreaterThanAndOrderLessThanEqualOrderByOrderAsc(
            @Param("minDocumentOrder") int minDocumentOrder,
            @Param("maxDocumentOrder") int maxDocumentOrder,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.category c " +
            "WHERE c.id = :categoryId " +
            "AND c.member.id = :memberId " +
            "ORDER BY d.updatedAt DESC")
    List<Document> findAllByCategoryIdAndMemberIdOrderByUpdatedAtDesc(
            @Param("categoryId") Long categoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.category c " +
            "WHERE c.id = :categoryId " +
            "AND c.member.id = :memberId " +
            "ORDER BY d.name ASC")
    List<Document> findAllByCategoryIdAndMemberIdOrderByNameAsc(
            @Param("categoryId") Long categoryId,
            @Param("memberId") Long memberId
    );
}

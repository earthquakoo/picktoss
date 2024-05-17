package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d FROM Document d JOIN d.category c WHERE c.id = :categoryId AND c.member.id = :memberId ORDER BY d.createdAt DESC")
    List<Document> findAllByCategoryIdAndMemberId(
            @Param("categoryId") Long categoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d JOIN d.category c WHERE d.id = :documentId AND c.member.id = :memberId")
    Optional<Document> findByDocumentIdAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d LEFT JOIN d.category c WHERE c.member.id = :memberId ORDER BY d.createdAt DESC")
    List<Document> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT MAX(d.order) FROM Document d JOIN d.category c WHERE c.id = :categoryId AND c.member.id = :memberId")
    Integer findLastOrderByCategoryIdAndMemberId(
            @Param("categoryId") Long categoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d LEFT JOIN d.category c WHERE c.member.id = :memberId AND d.name LIKE CONCAT('%', :word, '%')")
    Optional<Document> findBySpecificWord(
            @Param("memberId") Long memberId,
            @Param("word") String word
    );

    @Query("SELECT d FROM Document d JOIN d.category c WHERE c.id = :categoryId AND c.member.id = :memberId ORDER BY d.name DESC")
    List<Document> findAllByCategoryIdAndMemberIdOrderByName(
            @Param("categoryId") Long categoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d JOIN d.category c WHERE c.id = :categoryId AND c.member.id = :memberId ORDER BY d.updatedAt DESC")
    List<Document> findAllByCategoryIdAndMemberIdOrderByUpdatedAt(
            @Param("categoryId") Long categoryId,
            @Param("memberId") Long memberId
    );

    @Transactional
    @Modifying
//    @Query("UPDATE Document d SET d.order = d.order + 1 WHERE d.category.member.id = :memberId AND d.order >= :minDocumentOrder AND d.order < :maxDocumentOrder")
    @Query("UPDATE Document d SET d.order = d.order + 1 WHERE d.order >= :minDocumentOrder AND d.order < :maxDocumentOrder AND EXISTS (SELECT 1 FROM Document d2 JOIN d2.category c WHERE c.member.id = :memberId)")
    void updatePlusDocumentOrderByPreOrderGreaterThanAfterOrder(
            @Param("minDocumentOrder") int minDocumentOrder,
            @Param("maxDocumentOrder") int maxDocumentOrder,
            @Param("memberId") Long memberId
    );

    @Transactional
    @Modifying
//    @Query("UPDATE Document d SET d.order = d.order - 1 WHERE d.category.member.id = :memberId AND d.order > :minDocumentOrder AND d.order <= :maxDocumentOrder)")
    @Query("UPDATE Document d SET d.order = d.order - 1 WHERE d.order > :minDocumentOrder AND d.order <= :maxDocumentOrder AND EXISTS (SELECT 1 FROM Document d2 JOIN d2.category c WHERE c.member.id = :memberId)")
    void updateMinusDocumentOrderByPreOrderLessThanAfterOrder(
            @Param("minDocumentOrder") int minDocumentOrder,
            @Param("maxDocumentOrder") int maxDocumentOrder,
            @Param("memberId") Long memberId
    );

    @Transactional
    @Modifying
//    @Query("UPDATE Document d SET d.order = d.order - 1 WHERE d.category.member.id = :memberId AND d.order > :deletedOrder)")
    @Query("UPDATE Document d SET d.order = d.order - 1 WHERE d.order > :deletedOrder AND EXISTS (SELECT 1 FROM Document d2 JOIN d2.category c WHERE c.member.id = :memberId)")
    void updateMinusDocumentOrderByDeletedOrder(
            @Param("deletedOrder") int deletedOrder,
            @Param("memberId") Long memberId
    );
}

package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT MAX(d.order) FROM Document d JOIN d.category c WHERE c.member.id = :memberId")
    Integer findLastOrderByMemberId(@Param("memberId") Long memberId);
}

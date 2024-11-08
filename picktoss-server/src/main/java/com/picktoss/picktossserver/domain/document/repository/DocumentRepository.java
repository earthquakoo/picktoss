package com.picktoss.picktossserver.domain.document.repository;

import com.picktoss.picktossserver.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("SELECT DISTINCT d FROM Document d " +
            "JOIN FETCH d.directory di " +
            "WHERE d.id = :documentId " +
            "AND di.member.id = :memberId")
    Optional<Document> findByDocumentIdAndMemberId(
            @Param("documentId") Long documentId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN FETCH d.directory di " +
            "WHERE di.member.id = :memberId " +
            "ORDER BY d.createdAt DESC")
    List<Document> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN FETCH d.directory di " +
            "WHERE d.id IN :documentIds " +
            "AND di.member.id = :memberId")
    List<Document> findByDocumentIdsInAndMemberId(
            @Param("documentIds") List<Long> documentIds,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.directory di " +
            "WHERE di.id = :directoryId " +
            "AND di.member.id = :memberId " +
            "ORDER BY d.updatedAt DESC")
    List<Document> findAllByDirectoryIdAndMemberIdOrderByUpdatedAtDesc(
            @Param("directoryId") Long directoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.directory di " +
            "WHERE di.id = :directoryId " +
            "AND di.member.id = :memberId " +
            "ORDER BY d.createdAt DESC")
    List<Document> findAllByDirectoryIdAndMemberIdOrderByCreatedAtDesc(
            @Param("directoryId") Long directoryId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.directory di " +
            "WHERE di.member.id = :memberId " +
            "ORDER BY d.updatedAt DESC")
    List<Document> findAllByMemberIdOrderByUpdatedAtDesc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN d.directory di " +
            "WHERE di.member.id = :memberId " +
            "ORDER BY d.createdAt DESC")
    List<Document> findAllByMemberIdOrderByCreatedAtDesc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT d FROM Document d " +
            "JOIN FETCH d.directory di " +
            "JOIN FETCH d.quizzes q " +
            "WHERE di.member.id = :memberId")
    List<Document> findAllWithDirectoryAndQuizzes(
            @Param("memberId") Long memberId
    );
}

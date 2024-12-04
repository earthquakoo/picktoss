package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.global.enums.collection.CollectionField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "ORDER BY c.updatedAt DESC")
    List<Collection> findAllOrderByUpdatedAtDesc();

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "WHERE c.collectionField IN :collectionFields " +
            "ORDER BY c.updatedAt DESC")
    List<Collection> findAllByCollectionDomainsAndUpdatedAt(
            @Param("collectionFields") List<CollectionField> collectionFields
    );

    @Query("SELECT c FROM Collection c " +
            "WHERE c.id = :collectionId")
    Optional<Collection> findCollectionById(
            @Param("collectionId") Long collectionId
    );

    @Query("SELECT c FROM Collection c " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "JOIN FETCH cq.quiz q " +
            "WHERE c.id = :collectionId")
    Optional<Collection> findCollectionWithCollectionQuizByCollectionId(
            @Param("collectionId") Long collectionId
    );

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks cb " +
            "WHERE cb.member.id = :memberId")
    List<Collection> findAllByMemberIdAndBookmarked(
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "LEFT JOIN FETCH c.collectionSolvedRecords " +
            "JOIN FETCH cq.quiz q " +
            "JOIN FETCH c.member m " +
            "WHERE m.id = :memberId")
    List<Collection> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Collection c " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "LEFT JOIN FETCH c.collectionBookmarks cb " +
            "WHERE c.id = :collectionId " +
            "AND c.member.id = :memberId")
    Optional<Collection> findCollectionByCollectionIdAndMemberId(
            @Param("collectionId") Long collectionId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Collection c " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "LEFT JOIN FETCH c.collectionBookmarks cb " +
            "LEFT JOIN FETCH c.collectionSolvedRecords " +
            "JOIN FETCH cq.quiz q " +
            "WHERE c.id = :collectionId " +
            "AND c.member.id = :memberId")
    Optional<Collection> findCollectionWithSolvedRecordAndBookmarkAndQuizzesByCollectionIdAndMemberId(
            @Param("collectionId") Long collectionId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Collection c " +
            "WHERE c.name LIKE %:keyword%")
    List<Collection> findByCollectionContaining(
            @Param("keyword") String keyword
    );
}

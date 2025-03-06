package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks " +
            "LEFT JOIN FETCH c.collectionQuizzes cq " +
            "JOIN FETCH cq.quiz q " +
            "ORDER BY c.updatedAt DESC")
    List<Collection> findAllOrderByUpdatedAtDesc();

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks " +
            "LEFT JOIN FETCH c.collectionQuizzes cq " +
            "JOIN FETCH cq.quiz q " +
            "WHERE c.collectionCategory IN :collectionCategories " +
            "ORDER BY c.updatedAt DESC")
    List<Collection> findAllByCollectionDomainsAndUpdatedAt(
            @Param("collectionCategories") List<CollectionCategory> collectionCategories
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
            "JOIN FETCH c.collectionQuizzes cq " +
            "JOIN FETCH cq.quiz q " +
            "WHERE cb.member.id = :memberId")
    List<Collection> findAllByMemberIdAndBookmarked(
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "JOIN FETCH cq.quiz q " +
            "JOIN FETCH c.member m " +
            "WHERE m.id = :memberId")
    List<Collection> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks cb " +
            "WHERE (cb.member.id = :memberId OR c.member.id = :memberId)")
    List<Collection> findAllByMemberIdOrBookmarked(
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
            "JOIN FETCH cq.quiz q " +
            "WHERE c.id = :collectionId")
    Optional<Collection> findCollectionWithSolvedRecordAndBookmarkAndQuizzesByCollectionId(
            @Param("collectionId") Long collectionId
    );

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks cb " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "JOIN FETCH cq.quiz q " +
            "WHERE c.name LIKE %:keyword%")
    List<Collection> findByCollectionContaining(
            @Param("keyword") String keyword
    );

    /**
     * Admin-related collection repo
     */

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "JOIN FETCH cq.quiz q " +
            "JOIN FETCH c.member m")
    List<Collection> findAllWithAdminPrivileges();

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks cb " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "JOIN FETCH cq.quiz q " +
            "WHERE c.name LIKE %:keyword%")
    List<Collection> findByCollectionByKeyword(
            @Param("keyword") String keyword
    );

    @Query("SELECT c FROM Collection c " +
            "LEFT JOIN FETCH c.collectionBookmarks cb " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "JOIN FETCH cq.quiz q " +
            "WHERE c.member.name LIKE %:memberName%")
    List<Collection> findByCollectionByMemberName(
            @Param("memberName") String memberName
    );
}

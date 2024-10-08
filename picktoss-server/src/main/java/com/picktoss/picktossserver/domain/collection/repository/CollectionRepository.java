package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.global.enums.CollectionDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("SELECT c FROM Collection c " +
            "WHERE c.member.id = :memberId " +
            "ORDER BY c.updatedAt DESC")
    List<Collection> findAllOrderByUpdatedAtDesc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Collection c " +
            "WHERE c.collectionDomain IN :collectionDomains " +
            "AND c.member.id = :memberId")
    List<Collection> findAllByCollectionDomains(
            @Param("collectionDomains") List<CollectionDomain> collectionDomains,
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Collection c " +
            "WHERE c.id = :collectionId")
    Optional<Collection> findCollectionById(
            @Param("collectionId") Long collectionId
    );

    @Query("SELECT c FROM Collection c " +
            "WHERE c.id = :collectionId " +
            "AND c.member.id = :memberId")
    Optional<Collection> findCollectionByIdAndMemberId(
            @Param("collectionId") Long collectionId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Collection c " +
            "WHERE c.name LIKE %:keyword% " +
            "AND c.member.id = :memberId")
    List<Collection> findByCollectionContaining(
            @Param("keyword") String keyword,
            @Param("memberId") Long memberId
    );
}

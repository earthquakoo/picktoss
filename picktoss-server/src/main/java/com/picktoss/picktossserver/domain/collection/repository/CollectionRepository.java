package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    @Query("SELECT c FROM Collection c " +
            "ORDER BY c.createdAt DESC")
    List<Collection> findAllOrderByCreatedAtDesc();

    @Query("SELECT c FROM Collection c " +
            "WHERE c.id = :collectionId " +
            "AND c.member.id = :memberId")
    Optional<Collection> findCollectionByIdAndMemberId(
            @Param("collectionId") Long collectionId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM Collection c " +
            "WHERE c.name LIKE %:keyword%")
    List<Collection> findByCollectionContaining(
            @Param("keyword") String keyword
    );
}

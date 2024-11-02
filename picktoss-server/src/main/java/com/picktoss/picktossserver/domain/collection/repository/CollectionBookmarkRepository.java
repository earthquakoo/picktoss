package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionBookmarkRepository extends JpaRepository<CollectionBookmark, Long> {

    @Query("SELECT c FROM CollectionBookmark cb " +
            "JOIN cb.collection c " +
            "WHERE cb.member.id = :memberId")
    List<Collection> findAllCollectionByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("SELECT c FROM CollectionBookmark cb " +
            "JOIN cb.collection c " +
            "WHERE cb.member.id = :memberId " +
            "AND c.id = :collectionId")
    Optional<CollectionBookmark> findByMemberIdAndCollectionId(
            @Param("memberId") Long memberId,
            @Param("collectionId") Long collectionId
    );
}

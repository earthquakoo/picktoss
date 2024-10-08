package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionBookmarkRepository extends JpaRepository<CollectionBookmark, Long> {

    @Query("SELECT c FROM CollectionBookmark cb " +
            "JOIN cb.collection c " +
            "WHERE cb.member.id = :memberId")
    List<Collection> findCollectionByMemberId(
            @Param("memberId") Long memberId);
}

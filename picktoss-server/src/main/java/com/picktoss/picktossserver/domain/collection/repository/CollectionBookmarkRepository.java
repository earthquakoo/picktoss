package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import com.picktoss.picktossserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollectionBookmarkRepository extends JpaRepository<CollectionBookmark, Long> {

//    @Query("SELECT c FROM CollectionBookmark cb " +
//            "JOIN FETCH cb.collection c " +
//            "WHERE cb.member.id = :memberId " +
//            "AND c.id = :collectionId")
//    Optional<CollectionBookmark> findByMemberIdAndCollectionId(
//            @Param("memberId") Long memberId,
//            @Param("collectionId") Long collectionId
//    );

    Optional<CollectionBookmark> findByMemberAndCollection(Member member, Collection collection);
}

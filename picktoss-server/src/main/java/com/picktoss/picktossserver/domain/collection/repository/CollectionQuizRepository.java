package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionQuizRepository extends JpaRepository<CollectionQuiz, Long> {

//    @Query("SELECT cq FROM CollectionQuiz cq " +
//            "WHERE cq.collection.id = :collectionId " +
//            "AND cq.member.id = :memberId")
//    List<CollectionQuiz> findAllByCollectionIdAndMemberId(
//            @Param("collectionId") Long collectionId,
//            @Param("memberId") Long memberId
//    );
}

package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionQuizRepository extends JpaRepository<CollectionQuiz, Long> {

    @Query("SELECT cq FROM CollectionQuiz cq " +
            "JOIN FETCH cq.collection c " +
            "JOIN FETCH cq.quiz q " +
            "LEFT JOIN FETCH c.collectionBookmarks cb " +
            "WHERE (cb.member.id = :memberId OR c.member.id = :memberId) " +
            "AND c.collectionCategory = :collectionCategory")
    List<CollectionQuiz> findQuizzesInCollectionByMemberIdOrBookmarkedAndCollectionField(
            @Param("memberId") Long memberId,
            @Param("collectionCategory") CollectionCategory collectionCategory
    );
}

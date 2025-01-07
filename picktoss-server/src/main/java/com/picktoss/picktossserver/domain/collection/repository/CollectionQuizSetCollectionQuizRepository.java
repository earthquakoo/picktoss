package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSetCollectionQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionQuizSetCollectionQuizRepository extends JpaRepository<CollectionQuizSetCollectionQuiz, Long> {

    @Query("SELECT cqscq FROM CollectionQuizSetCollectionQuiz cqscq " +
            "JOIN FETCH cqscq.collectionQuiz cq " +
            "JOIN FETCH cq.quiz q " +
            "LEFT JOIN FETCH q.options " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "JOIN FETCH cqscq.collectionQuizSet cqs " +
            "WHERE cqs.id = :quizSetId " +
            "AND cqs.member.id = :memberId")
    List<CollectionQuizSetCollectionQuiz> findAllByQuizSetIdAndMemberId(
            @Param("quizSetId") String quizSetId,
            @Param("memberId") Long memberId
    );

}

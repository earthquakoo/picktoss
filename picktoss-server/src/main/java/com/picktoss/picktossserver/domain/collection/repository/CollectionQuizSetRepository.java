package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CollectionQuizSetRepository extends JpaRepository<CollectionQuizSet, String> {

    @Query("SELECT cqs FROM CollectionQuizSet cqs " +
            "WHERE cqs.member.id = :memberId " +
            "AND cqs.solved = true " +
            "AND cqs.createdAt >= :startDateTime " +
            "AND cqs.createdAt <= :endDateTime")
    List<CollectionQuizSet> findAllByMemberIdAndSolvedTrueAndDateTime(
            @Param("memberId") Long memberId,
            @Param("startDateTime") LocalDateTime starDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT DISTINCT cqs FROM CollectionQuizSet cqs " +
            "JOIN FETCH cqs.collectionQuizSetCollectionQuizzes cqscq " +
            "JOIN FETCH cqscq.collectionQuiz cq " +
            "JOIN FETCH cq.quiz q " +
            "JOIN FETCH q.document d " +
            "JOIN FETCH d.directory dir " +
            "WHERE cqs.member.id = :memberId " +
            "AND cqs.id = :collectionQuizSetId")
    Optional<CollectionQuizSet> findCollectionQuizSetByMemberIdAndQuizSetId(
            @Param("memberId") Long memberId,
            @Param("collectionQuizSetId") String collectionQuizSetId
    );

    @Query("SELECT cqs FROM CollectionQuizSet cqs " +
            "JOIN FETCH cqs.collectionQuizSetCollectionQuizzes cqscq " +
            "JOIN FETCH cqscq.collectionQuiz cq " +
            "JOIN FETCH cq.quiz q " +
            "WHERE cqs.member.id = :memberId " +
            "AND cqs.solved = true")
    List<CollectionQuizSet> findAllByMemberIdAndSolvedTrue(
            @Param("memberId") Long memberId
    );
}

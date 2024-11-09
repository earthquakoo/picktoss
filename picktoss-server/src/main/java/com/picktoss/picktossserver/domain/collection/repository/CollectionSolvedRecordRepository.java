package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.CollectionSolvedRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionSolvedRecordRepository extends JpaRepository<CollectionSolvedRecord, Long> {

    @Query("SELECT csr FROM CollectionSolvedRecord csr " +
            "LEFT JOIN FETCH csr.collectionSolvedRecordDetails " +
            "JOIN FETCH csr.collection c " +
            "JOIN FETCH c.collectionQuizzes cq " +
            "JOIN FETCH cq.quiz q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE csr.member.id = :memberId " +
            "AND csr.collection.id = :collectionId")
    Optional<CollectionSolvedRecord> findByMemberIdAndCollectionId(
            @Param("memberId") Long memberId,
            @Param("collectionId") Long collectionId
    );

    @Query("SELECT csr FROM CollectionSolvedRecord csr " +
            "JOIN FETCH csr.collection c " +
            "WHERE csr.member.id = :memberId")
    List<CollectionSolvedRecord> findAllByMemberId(
            @Param("memberId") Long memberId
    );
}

package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.CollectionSolvedRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionSolvedRecordRepository extends JpaRepository<CollectionSolvedRecord, Long> {

    @Query("SELECT csr FROM CollectionSolvedRecord csr " +
            "JOIN FETCH csr.collection c " +
            "WHERE csr.member.id = :memberId")
    List<CollectionSolvedRecord> findAllByMemberId(
            @Param("memberId") Long memberId
    );
}

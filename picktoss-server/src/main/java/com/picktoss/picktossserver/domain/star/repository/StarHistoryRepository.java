package com.picktoss.picktossserver.domain.star.repository;

import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.global.enums.star.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StarHistoryRepository extends JpaRepository<StarHistory, Long> {

    @Query("SELECT sh FROM StarHistory sh " +
            "JOIN FETCH sh.star s " +
            "WHERE s.member.id = :memberId " +
            "ORDER BY sh.createdAt")
    List<StarHistory> findAllByMemberIdOrderByCreatedAt(
            @Param("memberId") Long memberId
    );

    @Query("SELECT sh FROM StarHistory sh " +
            "JOIN FETCH sh.star s " +
            "WHERE s.member.id = :memberId " +
            "AND sh.transactionType = :transactionType")
    List<StarHistory> findAllByMemberIdAndTransactionTypeOrderByCreatedAt(
            @Param("memberId") Long memberId,
            @Param("transactionType") TransactionType transactionType
    );
}

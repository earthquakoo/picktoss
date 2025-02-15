package com.picktoss.picktossserver.domain.collection.repository;

import com.picktoss.picktossserver.domain.collection.entity.CollectionRandomQuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CollectionRandomQuizRecordRepository extends JpaRepository<CollectionRandomQuizRecord, Long> {

    @Query("SELECT crqr FROM CollectionRandomQuizRecord crqr " +
            "WHERE crqr.member.id = :memberId " +
            "AND crqr.createdAt >= :startOfDay " +
            "AND crqr.createdAt <= :endOfDay")
    Optional<CollectionRandomQuizRecord> findByMemberIdAndCreatedAtBetween(
            @Param("memberId") Long memberId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT crqr FROM CollectionRandomQuizRecord crqr " +
            "WHERE crqr.member.id = :memberId " +
            "AND crqr.createdAt >= :startOfDay " +
            "AND crqr.createdAt <= :endOfDay")
    List<CollectionRandomQuizRecord> findAllByMemberIdAndCreatedAtBetween(
            @Param("memberId") Long memberId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}

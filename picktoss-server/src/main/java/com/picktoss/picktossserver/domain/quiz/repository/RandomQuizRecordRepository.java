package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.RandomQuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RandomQuizRecordRepository extends JpaRepository<RandomQuizRecord, Long> {

    @Query("SELECT rqr FROM RandomQuizRecord rqr " +
            "WHERE rqr.member.id = :memberId " +
            "AND rqr.createdAt >= :startOfDay " +
            "AND rqr.createdAt <= :endOfDay")
    Optional<RandomQuizRecord> findRandomQuizRecordByMemberId(
            @Param("memberId") Long memberId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}

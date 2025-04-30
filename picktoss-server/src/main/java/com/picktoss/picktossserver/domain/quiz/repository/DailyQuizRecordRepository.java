package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.DailyQuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyQuizRecordRepository extends JpaRepository<DailyQuizRecord, Long> {

    @Query("SELECT dqr FROM DailyQuizRecord dqr " +
            "WHERE dqr.member.id = :memberId " +
            "AND dqr.solvedDate = :solvedDate")
    Optional<DailyQuizRecord> findByMemberIdAndSolvedDate(
            @Param("memberId") Long memberId,
            @Param("solvedDate") LocalDate solvedDate
    );

    @Query("SELECT dqr FROM DailyQuizRecord dqr " +
            "WHERE dqr.member.id = :memberId " +
            "AND dqr.isDailyQuizComplete = true " +
            "AND dqr.solvedDate = :solvedDate " +
            "ORDER BY dqr.solvedDate")
    List<DailyQuizRecord> findAllByMemberIdAndIsDailyQuizCompleteTrueAndSolvedDateOrderBySolvedDate(
            @Param("memberId") Long memberId,
            @Param("solvedDate") LocalDate solvedDate
    );

    @Query("SELECT dqr FROM DailyQuizRecord dqr " +
            "LEFT JOIN FETCH dqr.dailyQuizRecordDetails dqrd " +
            "WHERE dqr.member.id = :memberId " +
            "ORDER BY dqr.solvedDate DESC")
    List<DailyQuizRecord> findAllByMemberIdOrderBySolvedDateDesc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT dqr FROM DailyQuizRecord dqr " +
            "WHERE dqr.member.id = :memberId " +
            "AND dqr.isDailyQuizComplete = true " +
            "ORDER BY dqr.solvedDate DESC")
    List<DailyQuizRecord> findAllByMemberIdAndIsDailyQuizCompleteTrueOrderBySolvedDateDesc(
            @Param("memberId") Long memberId
    );

    @Query("SELECT dqr FROM DailyQuizRecord dqr " +
            "JOIN FETCH dqr.dailyQuizRecordDetails " +
            "WHERE dqr.id = :dailyQuizRecordId " +
            "AND dqr.member.id = :memberId")
    Optional<DailyQuizRecord> findByDailyQuizRecordIdAndMemberId(
            @Param("dailyQuizRecordId") Long dailyQuizRecordId,
            @Param("memberId") Long memberId
    );
}

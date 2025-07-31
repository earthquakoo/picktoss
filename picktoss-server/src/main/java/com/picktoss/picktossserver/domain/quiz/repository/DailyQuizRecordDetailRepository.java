package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.DailyQuizRecordDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DailyQuizRecordDetailRepository extends JpaRepository<DailyQuizRecordDetail, Long> {

    @Query("SELECT dqrd FROM DailyQuizRecordDetail dqrd " +
            "JOIN FETCH dqrd.dailyQuizRecord dqr " +
            "JOIN FETCH dqrd.quiz q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE dqr.id = :dailyQuizRecordId " +
            "AND dqr.member.id = :memberId")
    List<DailyQuizRecordDetail> findAllByDailyQuizRecordIdAndMemberId(
            @Param("dailyQuizRecordId") Long dailyQuizRecordId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT dqrd FROM DailyQuizRecordDetail dqrd " +
            "JOIN FETCH dqrd.dailyQuizRecord dqr " +
            "JOIN FETCH dqrd.quiz q " +
            "WHERE dqr.member.id = :memberId " +
            "AND dqr.solvedDate >= :startDate " +
            "AND dqr.solvedDate <= :endDate")
    List<DailyQuizRecordDetail> findAllByMemberIdAndDate(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime starDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT dqrd FROM DailyQuizRecordDetail dqrd " +
            "JOIN FETCH dqrd.dailyQuizRecord dqr " +
            "JOIN FETCH dqrd.quiz q " +
            "JOIN FETCH q.document d " +
            "WHERE dqr.member.id = :memberId " +
            "AND d.id = :documentId " +
            "AND dqr.solvedDate >= :oneMonthAgo")
    List<DailyQuizRecordDetail> findAllByMemberIdAndDocumentIdAndSolvedDateAfter(
            @Param("memberId") Long memberId,
            @Param("documentId") Long documentId,
            @Param("oneMonthAgo") LocalDateTime oneMonthAgo
    );
}

package com.picktoss.picktossserver.domain.quiz.repository;

import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizSetRepository extends JpaRepository<QuizSet, String> {

    @Query("SELECT qs FROM QuizSet qs WHERE qs.member.id = :memberId")
    List<QuizSet> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT qs FROM QuizSet qs " +
            "WHERE qs.id = :quizSetId " +
            "AND qs.member.id = :memberId")
    Optional<QuizSet> findByQuizSetIdAndMemberId(
            @Param("quizSetId") String quizSetId,
            @Param("memberId") Long memberId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.isTodayQuizSet = true " +
            "ORDER BY qs.createdAt DESC")
    List<QuizSet> findByMemberIdAndTodayQuizSetIsOrderByCreatedAt(
            @Param("memberId") Long memberId
    );

    @Query("SELECT qs FROM QuizSet qs " +
            "WHERE qs.member.id = :memberId " +
            "AND qs.createdAt >= :yesterdayStartTime " +
            "AND qs.createdAt <= :yesterdayEndTime " +
            "AND qs.isTodayQuizSet = true " +
            "ORDER BY qs.createdAt DESC")
    List<QuizSet> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndTodayQuizSetIs(
            @Param("memberId") Long memberId,
            @Param("yesterdayStartTime") LocalDateTime yesterdayStartTime,
            @Param("yesterdayEndTime") LocalDateTime yesterdayEndTime
    );
}

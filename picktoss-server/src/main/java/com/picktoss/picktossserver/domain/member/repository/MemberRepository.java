package com.picktoss.picktossserver.domain.member.repository;

import com.picktoss.picktossserver.domain.member.entity.Member;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByClientId(String id);

    Optional<Member> findByEmail(String email);

    @Query("SELECT m FROM Member m " +
            "WHERE m.isQuizNotificationEnabled = true")
    List<Member> findAllByIsQuizNotificationEnabled();

    @Query("SELECT m FROM Member m " +
            "LEFT JOIN FETCH m.collectionSolvedRecords " +
            "WHERE m.id = :memberId")
    Optional<Member> findMemberWithCollectionSolvedRecordByMemberId(
            @Param("memberId") Long memberId
    );

    //  "WHERE m.isQuizNotificationEnabled = true"
    @Query("SELECT MIN(m.id), MAX(m.id) FROM Member m " +
            "WHERE m.isQuizNotificationEnabled = true")
    Tuple findMinIdAndMaxIdAndIsQuizNotificationEnabled();
}

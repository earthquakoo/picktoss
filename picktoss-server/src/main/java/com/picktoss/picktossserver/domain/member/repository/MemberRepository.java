package com.picktoss.picktossserver.domain.member.repository;

import com.picktoss.picktossserver.domain.member.entity.Member;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByClientId(String id);

    //  "WHERE m.isQuizNotificationEnabled = true"
    @Query("SELECT MIN(m.id), MAX(m.id) FROM Member m " +
            "WHERE m.isQuizNotificationEnabled = true")
    Tuple findMinIdAndMaxIdAndIsQuizNotificationEnabled();

    @Query("SELECT m FROM Member m " +
            "where m.isQuizNotificationEnabled = true")
    List<Member> findAllByIsQuizNotificationEnabledTrue();
}

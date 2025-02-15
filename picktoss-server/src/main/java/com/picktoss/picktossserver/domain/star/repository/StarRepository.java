package com.picktoss.picktossserver.domain.star.repository;

import com.picktoss.picktossserver.domain.star.entity.Star;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StarRepository extends JpaRepository<Star, Long> {

    @Query("SELECT s FROM Star s " +
            "WHERE s.member.id = :memberId")
    Optional<Star> findByMemberId(
            @Param("memberId") Long memberId
    );
}

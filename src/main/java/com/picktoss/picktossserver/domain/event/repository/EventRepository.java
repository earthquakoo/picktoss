package com.picktoss.picktossserver.domain.event.repository;

import com.picktoss.picktossserver.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.member.id = :memberId")
    Optional<Event> findByMemberId(@Param("memberId") Long memberId);

}

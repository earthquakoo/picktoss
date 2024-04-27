package com.picktoss.picktossserver.domain.event.repository;

import com.picktoss.picktossserver.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}

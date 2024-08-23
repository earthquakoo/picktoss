package com.picktoss.picktossserver.domain.outbox.repository;

import com.picktoss.picktossserver.domain.outbox.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    @Query(value = "SELECT * FROM outbox FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<Outbox> findAllOutbox();
}
package com.picktoss.picktossserver.domain.outbox.repository;

import com.picktoss.picktossserver.domain.outbox.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    @Query(value = "SELECT o FROM Outbox o WHERE o.document.id = :documentId FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<Outbox> findByDocumentId(@Param("documentId") Long documentId);
}
package com.picktoss.picktossserver.domain.outbox.service;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.outbox.entity.Outbox;
import com.picktoss.picktossserver.domain.outbox.repository.OutboxRepository;
import com.picktoss.picktossserver.global.enums.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboxService {

    private final OutboxRepository outboxRepository;

    @Transactional
    public void createOutbox(OutboxStatus status, Document document) {
        Outbox outbox = Outbox.createOutbox(status, document);

        outboxRepository.save(outbox);
    }

    public List<Outbox> findAllOutbox() {
        return outboxRepository.findAllOutbox();
    }
}
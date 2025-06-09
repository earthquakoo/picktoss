package com.picktoss.picktossserver.core.eventlistener.listener.sqs;

import com.picktoss.picktossserver.core.eventlistener.event.sqs.SQSMessageEvent;
import com.picktoss.picktossserver.core.sqs.SqsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class SQSEventMessageListener {

    private final SqsProvider sqsProvider;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void testHandler(SQSMessageEvent event) {
        log.info("TransactionPhase.BEFORE_COMMIT ---> {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendMessageHandler(SQSMessageEvent event) {
        log.info("TransactionPhase.AFTER_COMMIT ---> {}", event);
        sqsProvider.sendMessage(event.getMemberId(), event.getS3Key(), event.getDocumentId(), event.getStarCount());
    }
}
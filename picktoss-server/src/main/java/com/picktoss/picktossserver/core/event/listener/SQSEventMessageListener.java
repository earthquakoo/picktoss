package com.picktoss.picktossserver.core.event.listener;

import com.picktoss.picktossserver.core.event.event.TransactionEvent;
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendMessageHandler(TransactionEvent event) {
        sqsProvider.sendMessage(event.getMemberId(), event.getS3Key(), event.getDocumentId(), event.getSubscriptionPlanType());
    }
}
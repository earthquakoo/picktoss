package com.picktoss.picktossserver.core.event.publisher;

import com.picktoss.picktossserver.core.event.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SQSEventMessagePublisher {

    private final ApplicationEventPublisher publisher;

    public void sqsEventMessagePublisher(TransactionEvent event) {
        publisher.publishEvent(event);
    }
}
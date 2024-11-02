package com.picktoss.picktossserver.core.event.publisher.sqs;

import com.picktoss.picktossserver.core.event.event.sqs.SQSMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SQSEventMessagePublisher {

    private final ApplicationEventPublisher publisher;

    public void sqsEventMessagePublisher(SQSMessageEvent event) {
        publisher.publishEvent(event);
    }
}
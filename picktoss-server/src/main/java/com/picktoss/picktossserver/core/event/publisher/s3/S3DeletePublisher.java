package com.picktoss.picktossserver.core.event.publisher.s3;

import com.picktoss.picktossserver.core.event.event.s3.S3DeleteEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3DeletePublisher {

    private final ApplicationEventPublisher publisher;

    public void s3DeletePublisher(S3DeleteEvent event) {
        publisher.publishEvent(event);
    }
}

package com.picktoss.picktossserver.core.event.publisher;

import com.picktoss.picktossserver.core.event.event.S3Event;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3UploadPublisher {

    private final ApplicationEventPublisher publisher;

    public void s3UploadPublisher(S3Event event) {
        publisher.publishEvent(event);
    }
}

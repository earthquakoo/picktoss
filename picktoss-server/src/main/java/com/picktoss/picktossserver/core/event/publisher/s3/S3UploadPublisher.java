package com.picktoss.picktossserver.core.event.publisher.s3;

import com.picktoss.picktossserver.core.event.event.s3.S3UploadEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3UploadPublisher {

    private final ApplicationEventPublisher publisher;

    public void s3UploadPublisher(S3UploadEvent event) {
        publisher.publishEvent(event);
    }
}

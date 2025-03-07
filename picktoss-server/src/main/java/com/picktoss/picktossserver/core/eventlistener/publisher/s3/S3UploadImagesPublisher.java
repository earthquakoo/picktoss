package com.picktoss.picktossserver.core.eventlistener.publisher.s3;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadImagesEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3UploadImagesPublisher {

    private final ApplicationEventPublisher publisher;

    public void s3UploadImagesPublisher(S3UploadImagesEvent event) {
        publisher.publishEvent(event);
    }
}

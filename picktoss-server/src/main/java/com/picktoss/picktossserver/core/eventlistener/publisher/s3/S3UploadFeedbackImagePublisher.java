package com.picktoss.picktossserver.core.eventlistener.publisher.s3;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadFeedbackImageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3UploadFeedbackImagePublisher {

    private final ApplicationEventPublisher publisher;

    public void s3UploadFeedbackImagePublisher(S3UploadFeedbackImageEvent event) {
        publisher.publishEvent(event);
    }
}

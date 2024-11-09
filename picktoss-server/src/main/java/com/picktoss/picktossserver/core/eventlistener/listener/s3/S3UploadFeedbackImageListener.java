package com.picktoss.picktossserver.core.eventlistener.listener.s3;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadFeedbackImageEvent;
import com.picktoss.picktossserver.core.s3.S3Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3UploadFeedbackImageListener {

    private final S3Provider s3Provider;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void s3UploadHandler(S3UploadFeedbackImageEvent event) {
        s3Provider.uploadFeedbackImage(event.getFiles(), event.getS3Keys());
    }
}

package com.picktoss.picktossserver.core.event.listener;

import com.picktoss.picktossserver.core.event.event.S3Event;
import com.picktoss.picktossserver.core.s3.S3Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3UploadListener {

    private final S3Provider s3Provider;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void s3UploadHandler(S3Event event) {
        s3Provider.uploadFile(event.getFile(), event.getS3Key());
    }
}

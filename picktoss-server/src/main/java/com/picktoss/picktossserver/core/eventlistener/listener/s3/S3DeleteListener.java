package com.picktoss.picktossserver.core.eventlistener.listener.s3;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3DeleteEvent;
import com.picktoss.picktossserver.core.s3.S3Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3DeleteListener {

    private final S3Provider s3Provider;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void s3DeleteHandler(S3DeleteEvent event) {
        s3Provider.deleteFile(event.getS3Key());
    }
}

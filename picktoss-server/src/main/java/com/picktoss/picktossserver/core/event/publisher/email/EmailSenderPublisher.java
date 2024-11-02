package com.picktoss.picktossserver.core.event.publisher.email;

import com.picktoss.picktossserver.core.event.event.email.EmailSenderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSenderPublisher {

    private final ApplicationEventPublisher publisher;

    public void emailSenderPublisher(EmailSenderEvent event) {
        publisher.publishEvent(event);
    }
}

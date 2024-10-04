package com.picktoss.picktossserver.core.event.listener;

import com.picktoss.picktossserver.core.email.MailgunEmailSenderManager;
import com.picktoss.picktossserver.core.event.event.EmailSenderEvent;
import com.picktoss.picktossserver.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailSenderListener {

    private final MailgunEmailSenderManager mailgunEmailSenderManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void emailSenderHandler(EmailSenderEvent emailSenderEvent) throws InterruptedException {
        List<Member> members = emailSenderEvent.getMembers();
        for (Member member : members) {
            mailgunEmailSenderManager.sendTodayQuizSet(member.getEmail(), member.getName());
            Thread.sleep(100);
        }
    }
}
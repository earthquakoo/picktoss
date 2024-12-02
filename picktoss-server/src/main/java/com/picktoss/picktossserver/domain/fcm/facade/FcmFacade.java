package com.picktoss.picktossserver.domain.fcm.facade;

import com.picktoss.picktossserver.domain.fcm.service.FcmService;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmFacade {

    private final MemberService memberService;
    private final FcmService fcmService;

    public void saveFcmToken(Long memberId, String fcmToken) {
        fcmService.saveFcmToken(memberId, fcmToken);
    }

    public void sendByToken(String title, String body, String content, Long memberId) {
        fcmService.sendByToken(title, body, content, memberId);
    }

    public void pushNotification(Long memberId, String content) {
        fcmService.pushNotification(memberId, content);
    }
}

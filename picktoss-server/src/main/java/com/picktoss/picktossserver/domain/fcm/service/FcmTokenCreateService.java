package com.picktoss.picktossserver.domain.fcm.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.fcm.entity.FcmToken;
import com.picktoss.picktossserver.domain.fcm.repository.FcmTokenRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTokenCreateService {

    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createFcmToken(Long memberId, String token) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        Optional<FcmToken> optionalFcmToken = fcmTokenRepository.findByMemberId(memberId);
        if (optionalFcmToken.isPresent()) {
            FcmToken fcmToken = optionalFcmToken.get();
            fcmToken.updateFcmToken(token);
            return ;
        }

        FcmToken fcmToken = FcmToken.createFcmToken(token, member);
        fcmTokenRepository.save(fcmToken);
    }
}

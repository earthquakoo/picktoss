package com.picktoss.picktossserver.domain.fcm.service;

import com.picktoss.picktossserver.domain.fcm.repository.FcmTokenRepository;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTokenReadService {

    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;

    public boolean isFcmTokenRegistered(Long memberId) {
        // findBy 대신 existsBy 사용 (성능 최적화)
        return fcmTokenRepository.existsByMemberId(memberId);
    }
}

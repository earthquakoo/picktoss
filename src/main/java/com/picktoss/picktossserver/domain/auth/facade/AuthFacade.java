package com.picktoss.picktossserver.domain.auth.facade;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.GoogleMemberDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.KakaoMemberDto;
import com.picktoss.picktossserver.domain.auth.service.AuthService;
import com.picktoss.picktossserver.domain.event.service.EventService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import com.picktoss.picktossserver.global.enums.SocialPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final EventService eventService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public JwtTokenDto login(String accessToken, SocialPlatform socialPlatform) {
        String userInfoJson = getUserInfo(accessToken, socialPlatform);
        if (socialPlatform == SocialPlatform.KAKAO) {
            String nickname = authService.generateUniqueName();
            KakaoMemberDto kakaoMemberDto = authService.transJsonToKakaoMemberDto(userInfoJson);
            return kakaoMemberCreate(kakaoMemberDto, nickname);
        } else {
            GoogleMemberDto googleMemberDto = authService.transJsonToGoogleMemberDto(userInfoJson);
            return googleMemberCreate(googleMemberDto);
        }
    }

    @Transactional
    public JwtTokenDto googleMemberCreate(GoogleMemberDto googleMemberDto) {
        Optional<Member> optionalMember = memberService.findMemberByClientId(googleMemberDto.getId());

        if (optionalMember.isEmpty()) {
            Member member = Member.builder()
                    .name(googleMemberDto.getName())
                    .clientId(googleMemberDto.getId())
                    .email(googleMemberDto.getEmail())
                    .isQuizNotificationEnabled(true)
                    .build();

            memberService.createMember(member);
            subscriptionService.createSubscription(member);
            eventService.attendanceCheck(member);
            return jwtTokenProvider.generateToken(member.getId());
        }
        Long memberId = optionalMember.get().getId();
        return jwtTokenProvider.generateToken(memberId);
    }

    @Transactional
    public JwtTokenDto kakaoMemberCreate(KakaoMemberDto kakaoMemberDto, String nickname) {
        Optional<Member> optionalMember = memberService.findMemberByClientId(kakaoMemberDto.getId());

        if (optionalMember.isEmpty()) {
            Member member = Member.builder()
                    .name(nickname)
                    .clientId(kakaoMemberDto.getId())
                    .isQuizNotificationEnabled(false)
                    .build();

            memberService.createMember(member);
            subscriptionService.createSubscription(member);
            eventService.attendanceCheck(member);
            return jwtTokenProvider.generateToken(member.getId());
        }
        Long memberId = optionalMember.get().getId();
        return jwtTokenProvider.generateToken(memberId);
    }

    public String getUserInfo(String accessToken, SocialPlatform socialPlatform) {
        return authService.getUserInfo(accessToken, socialPlatform);
    }

    @Transactional
    public void sendVerificationCode(String email) {
        authService.sendVerificationCode(email);
    }

    @Transactional
    public void verifyVerificationCode(String email, String verificationCode, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        authService.verifyVerificationCode(email, verificationCode, member);
    }
}

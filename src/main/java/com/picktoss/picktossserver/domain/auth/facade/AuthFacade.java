package com.picktoss.picktossserver.domain.auth.facade;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.GoogleMemberDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.KakaoMemberDto;
import com.picktoss.picktossserver.domain.auth.controller.response.LoginResponse;
import com.picktoss.picktossserver.domain.auth.service.AuthService;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.event.service.EventService;
import com.picktoss.picktossserver.domain.keypoint.service.KeyPointService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import com.picktoss.picktossserver.global.enums.CategoryTag;
import com.picktoss.picktossserver.global.enums.MemberRole;
import com.picktoss.picktossserver.global.enums.SocialPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.AVAILABLE_AI_PICK_COUNT;
import static com.picktoss.picktossserver.global.enums.CategoryTag.DEFAULT;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final EventService eventService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CategoryService categoryService;
    private final DocumentService documentService;
    private final KeyPointService keyPointService;

    @Transactional
    public LoginResponse login(String accessToken, SocialPlatform socialPlatform) {
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
    public LoginResponse googleMemberCreate(GoogleMemberDto googleMemberDto) {
        Optional<Member> optionalMember = memberService.findMemberByClientId(googleMemberDto.getId());

        if (optionalMember.isEmpty()) {
            Member member = Member.builder()
                    .name(googleMemberDto.getName())
                    .clientId(googleMemberDto.getId())
                    .socialPlatform(SocialPlatform.GOOGLE)
                    .email(googleMemberDto.getEmail())
                    .isQuizNotificationEnabled(true)
                    .aiPickCount(AVAILABLE_AI_PICK_COUNT)
                    .role(MemberRole.ROLE_USER)
                    .build();

            memberService.createMember(member);
            subscriptionService.createSubscription(member);
            eventService.createEvent(member);
            Category category = categoryService.createDefaultCategory(member);
            Document document = documentService.createDefaultDocument(category);
            keyPointService.createDefaultKeyPoint(document);
            JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
            return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), true);
        }
        Member member = optionalMember.get();
        JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
        return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), false);
    }

    @Transactional
    public LoginResponse kakaoMemberCreate(KakaoMemberDto kakaoMemberDto, String nickname) {
        Optional<Member> optionalMember = memberService.findMemberByClientId(kakaoMemberDto.getId());

        if (optionalMember.isEmpty()) {
            Member member = Member.builder()
                    .name(nickname)
                    .clientId(kakaoMemberDto.getId())
                    .socialPlatform(SocialPlatform.KAKAO)
                    .isQuizNotificationEnabled(false)
                    .aiPickCount(AVAILABLE_AI_PICK_COUNT)
                    .role(MemberRole.ROLE_USER)
                    .build();

            memberService.createMember(member);
            subscriptionService.createSubscription(member);
            eventService.createEvent(member);
            Category category = categoryService.createDefaultCategory(member);
            Document document = documentService.createDefaultDocument(category);
            keyPointService.createDefaultKeyPoint(document);
            JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
            return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), true);
        }
        Member member = optionalMember.get();
        JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
        return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), false);
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

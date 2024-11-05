package com.picktoss.picktossserver.domain.auth.facade;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.GoogleMemberDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.KakaoMemberDto;
import com.picktoss.picktossserver.domain.auth.controller.response.LoginResponse;
import com.picktoss.picktossserver.domain.auth.service.AuthService;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.service.DirectoryService;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.constant.MemberConstant;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.star.service.StarService;
import com.picktoss.picktossserver.global.enums.member.MemberRole;
import com.picktoss.picktossserver.global.enums.member.SocialPlatform;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentService documentService;
    private final DirectoryService directoryService;
    private final StarService starService;

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
                    .todayQuizCount(MemberConstant.DEFAULT_TODAY_QUIZ_COUNT)
                    .role(MemberRole.ROLE_USER)
                    .build();

            memberService.createMember(member);
            starService.createStarBySignUp(member);
            Directory directory = directoryService.createDefaultDirectory(member);
            documentService.createDefaultDocument(directory);
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
                    .todayQuizCount(MemberConstant.DEFAULT_TODAY_QUIZ_COUNT)
                    .role(MemberRole.ROLE_USER)
                    .build();

            memberService.createMember(member);
            starService.createStarBySignUp(member);
            Directory directory = directoryService.createDefaultDirectory(member);
            documentService.createDefaultDocument(directory);
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

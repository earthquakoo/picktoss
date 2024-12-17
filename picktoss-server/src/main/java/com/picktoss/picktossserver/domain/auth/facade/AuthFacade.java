package com.picktoss.picktossserver.domain.auth.facade;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.GoogleMemberDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.KakaoMemberDto;
import com.picktoss.picktossserver.domain.auth.controller.response.LoginResponse;
import com.picktoss.picktossserver.domain.auth.service.AuthService;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.service.DirectoryService;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.star.service.StarService;
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
    public LoginResponse login(String accessToken, SocialPlatform socialPlatform, String inviteLink) {
        Member member;

        String memberInfo = authService.getOauthAccessMemberInfo(accessToken, socialPlatform);

        if (socialPlatform == SocialPlatform.KAKAO) {
            KakaoMemberDto kakaoMemberDto = authService.transJsonToKakaoMemberDto(memberInfo);
            Optional<Member> optionalMember = memberService.findMemberByClientId(kakaoMemberDto.getId());

            if (optionalMember.isEmpty()) {
                String uniqueCode = authService.generateUniqueCode();
                String nickname = "Picktoss#" + uniqueCode;
                member = memberService.createKakaoMember(nickname, kakaoMemberDto.getId());
                initializeNewMember(member);

                if (inviteLink != null) {
                    authService.verifyInviteCode(inviteLink, member.getId());
                }
                JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
                return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), true);
            } else {
                member = optionalMember.get();
                JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
                return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), false);
            }
        } else if (socialPlatform == SocialPlatform.GOOGLE) {
            GoogleMemberDto googleMemberDto = authService.transJsonToGoogleMemberDto(memberInfo);
            Optional<Member> optionalMember = memberService.findMemberByClientId(googleMemberDto.getId());

            if (optionalMember.isEmpty()) {
                member = memberService.createGoogleMember(googleMemberDto.getName(), googleMemberDto.getId(), googleMemberDto.getEmail());
                if (inviteLink != null) {
                    authService.verifyInviteCode(inviteLink, member.getId());
                }
                initializeNewMember(member);
                JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
                return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), true);
            } else {
                member = optionalMember.get();
                JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
                return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), false);
            }

        } else {
            throw new CustomException(ErrorInfo.INVALID_SOCIAL_PLATFORM);
        }
    }

    @Transactional
    public Member registerGoogleMember(GoogleMemberDto googleMemberDto, String inviteLink) {
        Optional<Member> optionalMember = memberService.findMemberByClientId(googleMemberDto.getId());

        if (optionalMember.isEmpty()) {
            Member member = memberService.createGoogleMember(googleMemberDto.getName(), googleMemberDto.getId(), googleMemberDto.getEmail());
            if (inviteLink != null) {
                authService.verifyInviteCode(inviteLink, member.getId());
            }
            initializeNewMember(member);
            return member;
        }
        return optionalMember.get();
    }

    @Transactional
    public Member registerKakaoMember(KakaoMemberDto kakaoMemberDto, String inviteLink) {
        Optional<Member> optionalMember = memberService.findMemberByClientId(kakaoMemberDto.getId());

        if (optionalMember.isEmpty()) {
            String uniqueCode = authService.generateUniqueCode();
            String nickname = "Picktoss#" + uniqueCode;
            Member member = memberService.createKakaoMember(nickname, kakaoMemberDto.getId());
            initializeNewMember(member);

            if (inviteLink != null) {
                authService.verifyInviteCode(inviteLink, member.getId());
            }
            return member;
        }
        return optionalMember.get();
    }


    @Transactional
    public void initializeNewMember(Member member) {
        starService.createStarBySignUp(member);
        Directory directory = directoryService.createDefaultDirectory(member);
        documentService.createDefaultDocument(directory);
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

    public String createInviteLink(Long memberId) {
        return authService.createInviteLink(memberId);
    }

    public void verifyInviteCode(String inviteLink, Long memberId) {
        authService.verifyInviteCode(inviteLink, memberId);
    }

    public void checkInviteCodeBySignUp(Long memberId) {

    }
}

package com.picktoss.picktossserver.domain.auth.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.auth.dto.GoogleMemberDto;
import com.picktoss.picktossserver.domain.auth.dto.KakaoMemberDto;
import com.picktoss.picktossserver.domain.auth.dto.response.LoginResponse;
import com.picktoss.picktossserver.domain.auth.util.AuthUtil;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.star.constant.StarConstant;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.domain.star.repository.StarRepository;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.repository.SubscriptionRepository;
import com.picktoss.picktossserver.global.enums.member.SocialPlatform;
import com.picktoss.picktossserver.global.enums.star.Source;
import com.picktoss.picktossserver.global.enums.star.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthCreateService {

    private final StarRepository starRepository;
    private final StarHistoryRepository starHistoryRepository;
    private final MemberRepository memberRepository;
    private final DirectoryRepository directoryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthUtil authUtil;

    @Transactional
    public LoginResponse login(String accessToken, SocialPlatform socialPlatform) {
        String memberInfo = getOauthAccessMemberInfo(accessToken, socialPlatform);
        System.out.println("memberInfo = " + memberInfo);

        if (socialPlatform == SocialPlatform.KAKAO) {
            KakaoMemberDto kakaoMemberDto = authUtil.transJsonToKakaoMemberDto(memberInfo);
            Optional<Member> optionalMember = memberRepository.findByClientId(kakaoMemberDto.getId());

            if (optionalMember.isEmpty()) {
                Member member = createKakaoMember(kakaoMemberDto);

                createMemberStar(member);
                createDefaultDirectory(member);
                createMemberSubscription(member);

                JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
                return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), true);
            } else {
                Member member = optionalMember.get();
                JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
                return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), false);
            }
        } else if (socialPlatform == SocialPlatform.GOOGLE) {
            GoogleMemberDto googleMemberDto = authUtil.transJsonToGoogleMemberDto(memberInfo);
            Optional<Member> optionalMember = memberRepository.findByClientId(googleMemberDto.getId());

            if (optionalMember.isEmpty()) {
                Member member = createGoogleMember(googleMemberDto);

                createMemberStar(member);
                createDefaultDirectory(member);
                createMemberSubscription(member);

                JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
                return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), true);
            } else {
                Member member = optionalMember.get();
                JwtTokenDto jwtTokenDto = jwtTokenProvider.generateToken(member);
                return new LoginResponse(jwtTokenDto.getAccessToken(), jwtTokenDto.getAccessTokenExpiration(), false);
            }

        } else {
            throw new CustomException(ErrorInfo.INVALID_SOCIAL_PLATFORM);
        }
    }

    @Transactional
    private void createDefaultDirectory(Member member) {
        Directory directory = Directory.createDefaultDirectory(member);
        directoryRepository.save(directory);
    }

    @Transactional
    private Star createMemberStar(Member member) {
        Star star = Star.createStar(StarConstant.SIGN_UP_STAR, member);
        StarHistory starHistory = StarHistory.createStarHistory("회원 가입", StarConstant.SIGN_UP_STAR, StarConstant.SIGN_UP_STAR, TransactionType.DEPOSIT, Source.SIGN_UP, star);

        starRepository.save(star);
        starHistoryRepository.save(starHistory);
        return star;
    }

    @Transactional
    private void depositStarByInviteFriendReward(Star star) {
        StarHistory starHistory = star.depositStarByInviteFriendReward(star);
        starHistoryRepository.save(starHistory);
    }

    @Transactional
    private void createMemberSubscription(Member member) {
        Subscription subscription = Subscription.createSubscription(null, member);

        subscriptionRepository.save(subscription);
    }

    @Transactional
    private Member createKakaoMember(KakaoMemberDto kakaoMemberDto) {
        Member member = Member.createKakaoMember(kakaoMemberDto.getKakaoAccount().getProfile().getNickName(), kakaoMemberDto.getId(), kakaoMemberDto.getKakaoAccount().getEmail());
        memberRepository.save(member);

        return member;
    }

    @Transactional
    private Member createGoogleMember(GoogleMemberDto googleMemberDto) {
        Member member = Member.createGoogleMember(googleMemberDto.getName(), googleMemberDto.getId(), googleMemberDto.getEmail());
        memberRepository.save(member);

        return member;
    }

    private String getOauthAccessMemberInfo(String accessToken, SocialPlatform socialPlatform) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        final String getGoogleUserInfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo";
        final String getKakaoUserInfoUrl = "https://kapi.kakao.com/v2/user/me";

        if (socialPlatform == SocialPlatform.KAKAO) {
            ResponseEntity<String> response = restTemplate.exchange(getKakaoUserInfoUrl, HttpMethod.GET, request, String.class);
            return response.getBody();
        }

        ResponseEntity<String> response = restTemplate.exchange(getGoogleUserInfoUrl, HttpMethod.GET, request, String.class);
        return response.getBody();
    }
}

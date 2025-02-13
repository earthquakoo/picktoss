package com.picktoss.picktossserver.domain.auth.service;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.auth.dto.OauthResponseDto;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.repository.DirectoryRepository;
import com.picktoss.picktossserver.domain.member.dto.dto.MemberInfoDto;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.star.constant.StarConstant;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.domain.star.repository.StarRepository;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.repository.SubscriptionRepository;
import com.picktoss.picktossserver.global.enums.star.Source;
import com.picktoss.picktossserver.global.enums.star.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthTestService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final StarRepository starRepository;
    private final StarHistoryRepository starHistoryRepository;
    private final DirectoryRepository directoryRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${oauth.google.client_id}")
    private String oauthClientId;

    @Value("${oauth.google.client_secret}")
    private String oauthClientSecret;

    @Value("${oauth.google.redirect_uri}")
    private String redirectUri;

    @Transactional
    public JwtTokenDto createMember(MemberInfoDto memberInfoDto) {
        Optional<Member> optionalMember = memberRepository.findByClientId(memberInfoDto.getSub());

        if (optionalMember.isEmpty()) {
            Member member = memberInfoDto.toEntity();
            memberRepository.save(member);

            createMemberStarForTest(member);
            createDefaultDirectoryForTest(member);
            createMemberSubscriptionForTest(member);

            return jwtTokenProvider.generateToken(member);
        }
        Member member = optionalMember.get();
        return jwtTokenProvider.generateToken(member);
    }

    @Transactional
    private void createDefaultDirectoryForTest(Member member) {
        Directory directory = Directory.createDefaultDirectory(member);
        directoryRepository.save(directory);
    }

    @Transactional
    private void createMemberStarForTest(Member member) {
        Star star = Star.createStar(StarConstant.SIGN_UP_STAR, member);
        StarHistory starHistory = StarHistory.createStarHistory("회원 가입", StarConstant.SIGN_UP_STAR, StarConstant.SIGN_UP_STAR, TransactionType.DEPOSIT, Source.SIGN_UP, star);

        starRepository.save(star);
        starHistoryRepository.save(starHistory);
    }

    @Transactional
    private void createMemberSubscriptionForTest(Member member) {
        Subscription subscription = Subscription.createSubscription(null, member);

        subscriptionRepository.save(subscription);
    }

    public String getRedirectUri() {
        return String.format(
                "https://accounts.google.com/o/oauth2/auth?client_id=%s&response_type=code&redirect_uri=%s&scope=openid%%20email%%20profile",
                oauthClientId,
                redirectUri
        );
    }

    public String getOauthAccessToken(String accessCode) {
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, String> params = new HashMap<>();

        final String googleTokenRequestUrl = "https://oauth2.googleapis.com/token";

        params.put("code", accessCode);
        params.put("client_id", oauthClientId);
        params.put("client_secret", oauthClientSecret);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");

        ResponseEntity<OauthResponseDto> responseEntity = restTemplate.postForEntity(googleTokenRequestUrl, params, OauthResponseDto.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            System.out.println("responseEntity = " + responseEntity.getBody().getAccessToken());
            return responseEntity.getBody().getIdToken().split("\\.")[1];
        }
        return null;
    }
}

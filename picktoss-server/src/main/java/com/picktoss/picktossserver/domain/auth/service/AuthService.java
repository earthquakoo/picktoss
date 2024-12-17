package com.picktoss.picktossserver.domain.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picktoss.picktossserver.core.email.MailgunEmailSenderManager;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.auth.controller.dto.GoogleMemberDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.KakaoMemberDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.OauthResponseDto;
import com.picktoss.picktossserver.domain.auth.entity.EmailVerification;
import com.picktoss.picktossserver.domain.auth.repository.EmailVerificationRepository;
import com.picktoss.picktossserver.domain.member.controller.dto.MemberInfoDto;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.enums.member.SocialPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MailgunEmailSenderManager mailgunEmailSenderManager;
    private final EmailVerificationRepository emailVerificationRepository;
    private final S3Provider s3Provider;
    private final RedisUtil redisUtil;

    @Value("${oauth.google.client_id}")
    private String oauthClientId;

    @Value("${oauth.google.client_secret}")
    private String oauthClientSecret;

    @Value("${oauth.google.redirect_uri}")
    private String redirectUri;

    @Value("${email_verification.expire_seconds}")
    private long verificationExpireDurationSeconds;

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

    public String getOauthAccessMemberInfo(String accessToken, SocialPlatform socialPlatform) {
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

    /**
     * 이메일 인증코드 발송
     */
    @Transactional
    public void sendVerificationCode(String email) {
        // Send verification code
        String verificationCode = generateUniqueCode();
        mailgunEmailSenderManager.sendVerificationCode(email, verificationCode);

        // Upsert register verification entry (always make is_verified to False)
        Optional<EmailVerification> optionalEmailVerification = emailVerificationRepository.findByEmail(email);
        if (optionalEmailVerification.isPresent()) {
            EmailVerification emailVerification = optionalEmailVerification.get();

            emailVerification.updateVerificationCode(verificationCode);
            emailVerification.unverify();
            emailVerification.renewExpirationTimeFromNow(verificationExpireDurationSeconds);
        } else {
            EmailVerification newEmailVerification = EmailVerification.builder()
                    .email(email)
                    .verificationCode(verificationCode)
                    .isVerified(false)
                    .expirationTime(LocalDateTime.now().plusSeconds(verificationExpireDurationSeconds))
                    .build();

            emailVerificationRepository.save(newEmailVerification);
        }
    }

    /**
     * 이메일 인증코드 인증
     */
    @Transactional
    public void verifyVerificationCode(String email, String verificationCode, Member member) {
        Optional<EmailVerification> optionalEmailVerification = emailVerificationRepository.findByEmail(email);
        if (optionalEmailVerification.isEmpty()) {
            throw new CustomException(EMAIL_VERIFICATION_NOT_FOUND);
        }

        EmailVerification emailVerification = optionalEmailVerification.get();

        if (emailVerification.isVerified()) {
            throw new CustomException(EMAIL_ALREADY_VERIFIED);
        }

        if (!emailVerification.getVerificationCode().equals(verificationCode)) {
            throw new CustomException(INVALID_VERIFICATION_CODE);
        }

        if (emailVerification.isExpired()) {
            throw new CustomException(VERIFICATION_CODE_EXPIRED);
        }
        emailVerification.verify();
        member.updateMemberEmail(email);
    }

//    // 초대 코드 생성
//    public String createInviteLink(Long memberId) {
//        String initLink = "https://www.picktoss.com/invite/";
//        String uniqueCode = generateUniqueCode();
//
//        String memberIdKey = memberId.toString();
//
//        // 기존 초대 코드 조회
//        Map<Object, Object> hashData = redisUtil.getHashData(RedisConstant.REDIS_INVITE_CODE_PREFIX, memberIdKey);
//        if (!hashData.isEmpty()) {
//            String inviteCode = hashData.get("inviteCode").toString();
//            return initLink + inviteCode;
//        }
//
////        LocalDateTime createdAt = LocalDateTime.now();
//        List<Long> inviteMembers = new ArrayList<>();
//
//        HashMap<Object, Object> memberIdKeyData = new HashMap<>();
//        memberIdKeyData.put("inviteCode", uniqueCode);
//        memberIdKeyData.put("inviteMembers", inviteMembers);
////        memberIdKeyData.put("createdAt", createdAt);
////        memberIdKeyData.put("expiresAt", createdAt.plusDays(3));
//
//        HashMap<Object, Object> inviteCodeKeyData = new HashMap<>();
//        inviteCodeKeyData.put("inviteMemberId", memberId);
////        inviteCodeKeyData.put("createdAt", createdAt);
////        inviteCodeKeyData.put("expiresAt", createdAt.plusDays(3));
//
//        redisUtil.setHashData(RedisConstant.REDIS_INVITE_CODE_PREFIX, memberIdKey, memberIdKeyData, Duration.ofDays(3));
//        redisUtil.setHashData(RedisConstant.REDIS_INVITE_CODE_PREFIX, uniqueCode, inviteCodeKeyData, Duration.ofDays(3));
//
//        return initLink + uniqueCode;
//    }
//
//    public void verifyInviteCode(String inviteCode, Long memberId) {
//        Map<Object, Object> inviteCodeKeyData = redisUtil.getHashData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode);
//
//        if (inviteCodeKeyData.isEmpty()) {
//            throw new CustomException(INVITE_LINK_EXPIRED_OR_NOT_FOUND);
//        }
//
//        Object inviteMemberIdObject = inviteCodeKeyData.get("inviteMemberId");
//        String memberIdKey = new ObjectMapper().convertValue(inviteMemberIdObject, new TypeReference<String>() {});
//
//        Map<Object, Object> memberIdKeyData = redisUtil.getHashData(RedisConstant.REDIS_INVITE_CODE_PREFIX, memberIdKey);
//        Object inviteMembersObject = memberIdKeyData.get("inviteMembers");
//        List<Long> inviteMembers = new ObjectMapper().convertValue(inviteMembersObject, new TypeReference<List<Long>>() {});
//        inviteMembers.add(memberId);
//
//        memberIdKeyData.put("inviteMemberIdList", inviteMembers);
//        redisUtil.setData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode, memberIdKeyData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);
//    }

    public String createInviteLink(Long memberId) {
        String initLink = "https://www.picktoss.com/invite/";

        String memberIdKey = memberId.toString();

        // 기존 초대 코드 조회
        Optional<Map> existingCode = redisUtil.getData(RedisConstant.REDIS_INVITE_CODE_PREFIX, memberIdKey, Map.class);
        if (existingCode.isPresent()) {
            Map map = existingCode.get();
            String inviteCode = map.get("inviteCode").toString();
            return initLink + inviteCode;
        }

        List<Long> inviteMemberIdList = new ArrayList<>();

        String uniqueCode = generateUniqueCode();
        String inviteLink = initLink + uniqueCode;

        LocalDateTime createdAt = LocalDateTime.now();

        Map<String, Object> memberIdKeyData = Map.of(
                "inviteCode", uniqueCode,
                "inviteMemberIdList", inviteMemberIdList,
                "createdAt", createdAt,
                "expiresAt", createdAt.plusDays(3)
        );

        Map<String, Object> inviteCodeKeyData = Map.of(
                "creatorMemberId", memberId,
                "inviteMemberIdList", inviteMemberIdList,
                "createdAt", createdAt,
                "expiresAt", createdAt.plusDays(3)
        );

        redisUtil.setData(RedisConstant.REDIS_INVITE_CODE_PREFIX, memberIdKey, memberIdKeyData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);
        redisUtil.setData(RedisConstant.REDIS_INVITE_CODE_PREFIX, uniqueCode, inviteCodeKeyData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);

        return inviteLink;
    }

    // 초대 코드 인증
    public void verifyInviteCode(String inviteCode, Long memberId) {
        Optional<Map> inviteCodeData = redisUtil.getData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode, Map.class);

        if (inviteCodeData.isEmpty()) {
            throw new CustomException(INVITE_LINK_EXPIRED_OR_NOT_FOUND);
        }

        Map inviteCodeKeyData = inviteCodeData.get();
        Object inviteMemberIdListObject = inviteCodeKeyData.get("inviteMemberIdList");

        ObjectMapper objectMapper = new ObjectMapper();

        List<Object> inviteMemberIdList = objectMapper.convertValue(inviteMemberIdListObject, new TypeReference<List<Object>>() {});
        inviteMemberIdList.add(memberId);

        inviteCodeKeyData.put("inviteMemberIdList", inviteMemberIdList);
        redisUtil.setData(RedisConstant.REDIS_INVITE_CODE_PREFIX, inviteCode, inviteCodeKeyData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);
    }

    public void checkInviteCodeBySignUp(Long memberId) {
        String memberIdKey = memberId.toString();

        Optional<Map> existingCode = redisUtil.getData(RedisConstant.REDIS_INVITE_CODE_PREFIX, memberIdKey, Map.class);
    }

    public String decodeIdToken(String idToken) {
        return new String(Base64.getDecoder().decode(idToken));
    }

    public MemberInfoDto transJsonToMemberInfoDto(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, MemberInfoDto.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public GoogleMemberDto transJsonToGoogleMemberDto(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, GoogleMemberDto.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public KakaoMemberDto transJsonToKakaoMemberDto(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, KakaoMemberDto.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateUniqueCode() {
        final int randomCodeLen = 6;
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder verificationCode = new StringBuilder(randomCodeLen);
        for (int i = 0; i < randomCodeLen; i++) {
            verificationCode.append(
                    chars.charAt(random.nextInt(chars.length()))
            );
        }
        return verificationCode.toString();
    }
}

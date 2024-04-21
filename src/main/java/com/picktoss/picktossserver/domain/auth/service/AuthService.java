package com.picktoss.picktossserver.domain.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picktoss.picktossserver.core.email.MailgunVerificationEmailManager;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.auth.controller.dto.OauthResponseDto;
import com.picktoss.picktossserver.domain.auth.entity.EmailVerification;
import com.picktoss.picktossserver.domain.auth.repository.EmailVerificationRepository;
import com.picktoss.picktossserver.domain.member.controller.dto.MemberInfoDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MailgunVerificationEmailManager mailgunVerificationEmailManager;
    private final EmailVerificationRepository emailVerificationRepository;

    @Value("${oauth.google.client_id}")
    private String oauthClientId;

    @Value("${oauth.google.client_secret}")
    private String oauthClientSecret;

    @Value("${oauth.google.redirect_uri}")
    private String redirectUri;

    @Getter
    @Value("${oauth.google.oauth_callback_response_url}")
    private String oauthCallbackResponseUrl;

    @Value("${email_verification.expire_seconds}")
    private long verificationExpireDurationSeconds;

    public HashMap<String, String> getRedirectUri() {
        String oauthUrl = String.format("https://accounts.google.com/o/oauth2/auth?client_id=%s&response_type=code&redirect_uri=%s&scope=openid%%20email%%20profile",
                oauthClientId, redirectUri);
        HashMap<String, String> urlMapping = new HashMap<>();
        urlMapping.put("oauth_url", oauthUrl);
        return urlMapping;
    }

//    public String getRedirectUri() {
//        return "https://accounts.google.com/o/oauth2/auth?" +
//                "client_id=" + oauthClientId + "&" +
//                "response_type=code&" +
//                "redirect_uri=" + redirectUri + "&" +
//                "scope=openid%20email%20profile";
//    }

    public String getOauthAccessToken(String accessCode) {
        RestTemplate restTemplate = new RestTemplate();
        HashMap<String, String> params = new HashMap<>();

        String googleTokenRequestUrl = "https://oauth2.googleapis.com/token";

        params.put("code", accessCode);
        params.put("client_id", oauthClientId);
        params.put("client_secret", oauthClientSecret);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");

        ResponseEntity<OauthResponseDto> responseEntity = restTemplate.postForEntity(googleTokenRequestUrl, params, OauthResponseDto.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody().getIdToken().split("\\.")[1];
        }
        return null;
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

    /**
     * 이메일 인증코드 발송
     */
    @Transactional
    public void sendVerificationCode(String email) {
        // Send verification code
        String verificationCode = generateVerificationCode();
        mailgunVerificationEmailManager.sendVerificationCode(email, verificationCode);

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
    public void verifyVerificationCode(String email, String verificationCode) {
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
    }


    private String generateVerificationCode() {
        int tokenLen = 6;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new SecureRandom();
        StringBuilder verificationCode = new StringBuilder(tokenLen);
        for (int i = 0; i < tokenLen; i++) {
            verificationCode.append(
                    chars.charAt(random.nextInt(chars.length()))
            );
        }
        return verificationCode.toString();
    }
}

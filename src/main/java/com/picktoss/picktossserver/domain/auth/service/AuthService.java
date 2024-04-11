package com.picktoss.picktossserver.domain.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picktoss.picktossserver.domain.auth.controller.dto.OauthResponseDto;
import com.picktoss.picktossserver.domain.member.controller.dto.MemberInfoDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${oauth.google.client_id}")
    private String oauthClientId;

    @Value("${oauth.google.client_secret}")
    private String oauthClientSecret;

    @Value("${oauth.google.redirect_uri}")
    private String redirectUri;

    @Getter
    @Value("${oauth.google.oauth_callback_response_url}")
    private String oauthCallbackResponseUrl;

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

}

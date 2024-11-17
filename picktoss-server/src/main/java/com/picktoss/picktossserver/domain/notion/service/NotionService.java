package com.picktoss.picktossserver.domain.notion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotionService {

    @Value("${oauth.notion.client_id}")
    private String notionClientId;

    @Value("${oauth.notion.client_secret}")
    private String notionClientSecret;

    @Value("${oauth.notion.redirect_uri}")
    private String notionRedirectUri;


    public String findNotionRedirectUri() {
        String notionAuthorizeUrl = "https://api.notion.com/v1/oauth/authorize";
        return UriComponentsBuilder.fromUriString(notionAuthorizeUrl)
                .queryParam("client_id", notionClientId)
                .queryParam("response_type", "code")
                .queryParam("owner", "user")
                .queryParam("redirect_uri", notionRedirectUri)
                .build()
                .toUriString();
    }

    public String findNotionOauthAccessToken(String accessCode) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(notionClientId, notionClientSecret); // Basic 인증 헤더 설정

        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("code", accessCode);
        params.add("redirect_uri", notionRedirectUri);
        params.add("grant_type", "authorization_code");

        String notionTokenRequestUrl = "https://api.notion.com/v1/oauth/token";

        HttpEntity<LinkedMultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(notionTokenRequestUrl, HttpMethod.POST, requestEntity, String.class);
        System.out.println("responseEntity = " + responseEntity.getBody());
        return responseEntity.getBody();
    }

    public String findNotionPages(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // Bearer 인증 헤더 설정
        headers.set("Notion-Version", "2022-06-28");

        String jsonBody = "{"
                + "\"filter\": {\"property\": \"object\", \"value\": \"page\"}"
                + "}";

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        String notionSearchUrl = "https://api.notion.com/v1/search";
        ResponseEntity<String> responseEntity = restTemplate.exchange(notionSearchUrl, HttpMethod.POST, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody(); // JSON 응답 반환
        } else {
            throw new RuntimeException("Failed to search Notion pages: " + responseEntity.getStatusCode());
        }
    }

    public String findNotionPage(String accessToken, String pageId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.set("Notion-Version", "2022-06-28");

        String notionBlocksUrl = "https://api.notion.com/v1/blocks/" + pageId + "/children";

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(notionBlocksUrl, HttpMethod.GET, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody(); // JSON 응답 반환
        } else {
            throw new RuntimeException("Failed to retrieve Notion page: " + responseEntity.getStatusCode());
        }
    }
}

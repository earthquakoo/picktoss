package com.picktoss.picktossserver.domain.notion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotionService {

    @Value("${oauth.notion.client_id}")
    private String notionClientId;

    @Value("${oauth.notion.client_secret}")
    private String notionClientSecret;

    private static final String notionRedirectUri = "http://localhost:8181/api/v2/notion/callback";

    public String getNotionRedirectUri() {
        return String.format(
                "https://api.notion.com/v1/oauth/authorize?client_id=%s&response_type=code&owner=user&redirect_uri=%s",
                notionClientId,
                notionRedirectUri
        );
    }

    public String getNotionOauthAccessToken(String accessCode) {
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

    public String getNotionPages(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // Bearer 인증 헤더 설정
        headers.set("Notion-Version", "2022-06-28");

        // 요청 바디 설정
//        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("filter", new LinkedMultiValueMap<String, String>() {{
//            add("property", "object");
//            add("value", "page");
//        }});
        String jsonBody = "{"
                + "\"filter\": {\"property\": \"object\", \"value\": \"page\"}"
                + "}";

        String notionSearchUrl = "https://api.notion.com/v1/search";

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(notionSearchUrl, HttpMethod.POST, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody(); // JSON 응답 반환
        } else {
            throw new RuntimeException("Failed to search Notion pages: " + responseEntity.getStatusCode());
        }
    }

    public String getNotionPage(String accessToken) {
        String pageId = "646a31a7-d68e-4ab2-ab16-46cd8af36558";
        RestTemplate restTemplate = new RestTemplate();

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // Bearer 인증 헤더 설정
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

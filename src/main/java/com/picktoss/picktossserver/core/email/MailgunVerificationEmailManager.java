package com.picktoss.picktossserver.core.email;

import com.picktoss.picktossserver.core.s3.S3Provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class MailgunVerificationEmailManager implements EmailManager {

    @Value("${mailgun.api_key}")
    private String mailgunApiKey;

    @Value("${mailgun.domain}")
    private String mailgunDomain;

    private static final String verificationHtmlPath = "emails/email_verification.html";

    @Override
    public void sendEmail(String recipientEmail, String subject, String content) {
        RestTemplate restTemplate = new RestTemplate();

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("api", mailgunApiKey);
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String encodedContent = UriUtils.encode(content, StandardCharsets.UTF_8);
        String encodedSubject = UriUtils.encode(subject, StandardCharsets.UTF_8);

        String body = String.format("from=%s&to=%s&subject=%s&html=%s",
                "picktoss <support@picktoss.com>",
                recipientEmail,
                encodedSubject,
                encodedContent);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(mailgunDomain, request, String.class);
        if (response.getStatusCode().value() != 200) {
            System.out.println("sendEmail error");
        }
    }

    public void sendVerificationCode(String recipientEmail, String verificationCode) {
        try {
            ClassPathResource classPathResource = new ClassPathResource(verificationHtmlPath);
            try (InputStream inputStream = classPathResource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                content = content.replace("__VERIFICATION_CODE__", verificationCode);
                sendEmail(recipientEmail, "[Picktoss] Please verify your email", content);
            }
        } catch (IOException e) {
            // TODO: ERROR 핸들링
            System.out.println("sendVerificationCode error");
        }
    }
}

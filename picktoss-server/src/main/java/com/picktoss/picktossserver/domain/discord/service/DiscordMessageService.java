package com.picktoss.picktossserver.domain.discord.service;

import com.picktoss.picktossserver.domain.discord.dto.DiscordMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordMessageService {

    @Value("${discord.feedback_webhook_url}")
    private String discordFeedbackWebhookUrl;

    public void sendDiscordWebhookMessage(DiscordMessage message) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json; utf-8");
            HttpEntity<DiscordMessage> messageEntity = new HttpEntity<>(message, httpHeaders);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> response = template.exchange(
                    discordFeedbackWebhookUrl,
                    HttpMethod.POST,
                    messageEntity,
                    String.class
            );

        } catch (Exception e) {
            log.error("에러 발생 :: " + e);
        }
    }

    public DiscordMessage createMessage() {
        return DiscordMessage.builder()
                .content("# 🚨 에러 발생 🚨")
                .embeds(
                        List.of(
                                DiscordMessage.Embed.builder()
                                        .title("ℹ️ 정보")
                                        .description(
                                                "### 🕖 발생 시간\n"
                                                        + LocalDateTime.now()
                                                        + "\n"
                                                        + "### 📄 Stack Trace\n"
                                                        + "```\n")
                                        .build()
                        )
                )
                .build();
    }
}

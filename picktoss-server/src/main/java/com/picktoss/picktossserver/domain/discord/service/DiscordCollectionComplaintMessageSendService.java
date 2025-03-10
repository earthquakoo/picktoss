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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordCollectionComplaintMessageSendService {

    @Value("${discord.collection_complaint_webhook_url}")
    private String discordCollectionComplaintWebhookUrl;

    public void sendDiscordWebhookCollectionComplaintMessage(DiscordMessage message) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json; utf-8");
            HttpEntity<DiscordMessage> messageEntity = new HttpEntity<>(message, httpHeaders);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> response = template.exchange(
                    discordCollectionComplaintWebhookUrl,
                    HttpMethod.POST,
                    messageEntity,
                    String.class
            );

        } catch (Exception e) {
            log.error("에러 발생 :: " + e);
        }
    }

    public DiscordMessage createCollectionComplaintMessage(List<String> collectionComplaintImageUrls, String collectionComplaintContent, Long memberId, String memberName) {
        List<DiscordMessage.Embed> embeds = new ArrayList<>();
        DiscordMessage.Embed embedDto = DiscordMessage.Embed.builder()
                .title("ℹ️ 정보")
                .description("### 🕖 발생 시간\n"
                        + LocalDateTime.now()
                        + "\n"
                        + "### 👤 사용자 정보\n"
                        + "사용자 아이디 : " + memberId.toString() + "\n"
                        + "사용자 닉네임 : " + memberName + "\n"
                        + "### 🚨 신고 내용\n"
                        + collectionComplaintContent)
                .build();
        embeds.add(embedDto);

        for (String collectionComplaintImageUrl : collectionComplaintImageUrls) {
            DiscordMessage.Embed embed = DiscordMessage.Embed.builder()
                    .title("📸 사진 ")
                    .description(collectionComplaintImageUrl)
                    .build();
            embeds.add(embed);
        }

        return DiscordMessage.builder()
                .content("## User Feedback")
                .embeds(embeds)
                .build();
    }
}

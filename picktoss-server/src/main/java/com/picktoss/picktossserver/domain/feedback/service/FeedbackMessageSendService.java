package com.picktoss.picktossserver.domain.feedback.service;

import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.discord.dto.DiscordMessage;
import com.picktoss.picktossserver.domain.discord.service.DiscordFeedbackMessageSendService;
import com.picktoss.picktossserver.domain.feedback.entity.Feedback;
import com.picktoss.picktossserver.domain.feedback.entity.FeedbackFile;
import com.picktoss.picktossserver.domain.feedback.repository.FeedbackFileRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackMessageSendService {

    private final S3Provider s3Provider;
    private final DiscordFeedbackMessageSendService discordFeedbackMessageSendService;
    private final FeedbackFileRepository feedbackFileRepository;

    public void sendFeedbackDiscordMessage(Long feedbackId) {
        List<FeedbackFile> feedbackFiles = feedbackFileRepository.findAllByFeedbackId(feedbackId);
        Feedback feedback = feedbackFiles.getFirst().getFeedback();
        Member member = feedback.getMember();

        List<String> feedbackImageUrls = new ArrayList<>();

        for (FeedbackFile feedbackFile : feedbackFiles) {
            String feedbackImageUrl = s3Provider.findImage(feedbackFile.getS3Key());
            feedbackImageUrls.add(feedbackImageUrl);
        }

        DiscordMessage discordMessage = discordFeedbackMessageSendService.createFeedbackMessage(feedbackImageUrls, feedback.getContent(), member.getId(), member.getName());
        discordFeedbackMessageSendService.sendDiscordWebhookFeedbackMessage(discordMessage);
    }
}

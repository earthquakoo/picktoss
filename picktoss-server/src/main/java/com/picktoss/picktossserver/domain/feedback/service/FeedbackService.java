package com.picktoss.picktossserver.domain.feedback.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadImagesEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadImagesPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.discord.service.DiscordMessageService;
import com.picktoss.picktossserver.domain.feedback.entity.Feedback;
import com.picktoss.picktossserver.domain.feedback.entity.FeedbackFile;
import com.picktoss.picktossserver.domain.feedback.repository.FeedbackFileRepository;
import com.picktoss.picktossserver.domain.feedback.repository.FeedbackRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.global.enums.feedback.FeedbackType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackFileRepository feedbackFileRepository;
    private final MemberRepository memberRepository;
    private final S3UploadImagesPublisher s3UploadImagesPublisher;
    private final DiscordMessageService discordMessageService;

    @Transactional
    public void createFeedback(List<MultipartFile> files, String title, String content, FeedbackType type, String email, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        String customS3Key = UUID.randomUUID().toString();
        String s3FolderPath = "picktoss-feedback-images/";

        List<String> s3Keys = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fullS3Key = s3FolderPath + customS3Key + "_" + fileName;
            s3Keys.add(fullS3Key);
        }


        Feedback feedback = Feedback.createFeedback(title, content, type, email, member);

        List<FeedbackFile> feedbackFiles = new ArrayList<>();
        for (String s3Key : s3Keys) {
            FeedbackFile feedbackFile = FeedbackFile.createFeedbackFile(s3Key, feedback);
            feedbackFiles.add(feedbackFile);
        }

        s3UploadImagesPublisher.s3UploadImagesPublisher(new S3UploadImagesEvent(files, s3Keys));
        feedbackRepository.save(feedback);
        feedbackFileRepository.saveAll(feedbackFiles);

//        DiscordMessage message = discordMessageService.createMessage();
//        discordMessageService.sendDiscordWebhookMessage(message);
    }
}

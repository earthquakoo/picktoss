package com.picktoss.picktossserver.domain.feedback.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadImagesEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadImagesPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
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

    @Transactional
    public Feedback createFeedback(List<MultipartFile> files, String title, String content, FeedbackType type, String email, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        Feedback feedback = Feedback.createFeedback(title, content, type, email, member);
        feedbackRepository.save(feedback);

        if (files != null && !files.isEmpty()) {
            createFeedbackFiles(files, feedback);
        }

        return feedback;
    }

    @Transactional
    private void createFeedbackFiles(List<MultipartFile> files, Feedback feedback) {
        List<String> s3Keys = new ArrayList<>();

        String customS3Key = UUID.randomUUID().toString();
        String s3FolderPath = "picktoss-feedback-images/";

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fullS3Key = s3FolderPath + customS3Key + "_" + fileName;
            s3Keys.add(fullS3Key);
        }

        List<FeedbackFile> feedbackFiles = new ArrayList<>();
        for (String s3Key : s3Keys) {
            FeedbackFile feedbackFile = FeedbackFile.createFeedbackFile(s3Key, feedback);
            feedbackFiles.add(feedbackFile);
        }

        s3UploadImagesPublisher.s3UploadImagesPublisher(new S3UploadImagesEvent(files, s3Keys));
        feedbackFileRepository.saveAll(feedbackFiles);
    }
}

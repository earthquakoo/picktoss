package com.picktoss.picktossserver.domain.feedback.facade;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadImagesEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadImagesPublisher;
import com.picktoss.picktossserver.domain.feedback.service.FeedbackService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
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
public class FeedbackFacade {

    private final FeedbackService feedbackService;
    private final MemberService memberService;

    private final S3UploadImagesPublisher s3UploadImagesPublisher;

    @Transactional
    public void createFeedback(List<MultipartFile> files, String title, String content, FeedbackType type, String email, Long memberId) {
        Member member = memberService.findMemberById(memberId);

        String s3Key = UUID.randomUUID().toString();
        String s3FolderPath = "picktoss-feedback-images/";

        List<String> s3Keys = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fullS3Key = s3FolderPath + s3Key + "_" + fileName;
            s3Keys.add(fullS3Key);
        }

        s3UploadImagesPublisher.s3UploadImagesPublisher(new S3UploadImagesEvent(files, s3Keys));
        feedbackService.createFeedback(title, content, s3Keys, type, email, member);
    }
}
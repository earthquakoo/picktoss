package com.picktoss.picktossserver.domain.feedback.facade;

import com.picktoss.picktossserver.core.event.event.s3.S3UploadEvent;
import com.picktoss.picktossserver.core.event.publisher.s3.S3UploadPublisher;
import com.picktoss.picktossserver.domain.feedback.service.FeedbackService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.global.enums.feedback.FeedbackType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackFacade {

    private final FeedbackService feedbackService;
    private final MemberService memberService;

    private final S3UploadPublisher s3UploadPublisher;

    @Transactional
    public void createFeedback(MultipartFile file, String title, String content, FeedbackType type, String email, Long memberId) {
        Member member = memberService.findMemberById(memberId);

        String s3Key = UUID.randomUUID().toString();

        s3UploadPublisher.s3UploadPublisher(new S3UploadEvent(file, s3Key));
        feedbackService.createFeedback(title, content, s3Key, type, email, member);
    }
}
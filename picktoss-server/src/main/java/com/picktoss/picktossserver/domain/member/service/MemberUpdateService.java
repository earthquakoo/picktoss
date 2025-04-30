package com.picktoss.picktossserver.domain.member.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.repository.CategoryRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.CATEGORY_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberUpdateService {

    private final MemberRepository memberRepository;
    private final S3UploadPublisher s3UploadPublisher;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void updateQuizNotification(Long memberId, boolean isQuizNotification) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateQuizNotification(isQuizNotification);
    }

    @Transactional
    public void updateMemberName(Long memberId, String name) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateMemberName(name);
    }

    @Transactional
    public void updateMemberCategory(Long memberId, Long categoryId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        member.updateMemberCategory(category);
    }

    @Transactional
    public void updateMemberImage(Long memberId, MultipartFile file) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        String s3FolderPath = "picktoss-member-images/";
        String s3Key = s3FolderPath + UUID.randomUUID();

        member.updateMemberImage(s3Key);

        s3UploadPublisher.s3UploadPublisher(new S3UploadEvent(file, s3Key));
    }
}

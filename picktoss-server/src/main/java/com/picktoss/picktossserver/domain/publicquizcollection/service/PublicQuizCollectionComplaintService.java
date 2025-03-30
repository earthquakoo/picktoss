package com.picktoss.picktossserver.domain.publicquizcollection.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadImagesEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadImagesPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollectionComplaint;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollectionComplaintFile;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionComplaintFileRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionComplaintRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicQuizCollectionComplaintService {

    private final MemberRepository memberRepository;
    private final S3UploadImagesPublisher s3UploadImagesPublisher;
    private final PublicQuizCollectionRepository publicQuizCollectionRepository;
    private final PublicQuizCollectionComplaintRepository publicQuizCollectionComplaintRepository;
    private final PublicQuizCollectionComplaintFileRepository publicQuizCollectionComplaintFileRepository;

    @Transactional
    public PublicQuizCollectionComplaint createPublicQuizCollectionComplaint(Long publicQuizCollectionId, String content, Long memberId, List<MultipartFile> files) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        PublicQuizCollection publicQuizCollection = publicQuizCollectionRepository.findById(publicQuizCollectionId)
                .orElseThrow(() -> new CustomException(ErrorInfo.PUBLIC_QUIZ_COLLECTION_NOT_FOUND));

        PublicQuizCollectionComplaint publicQuizCollectionComplaint = PublicQuizCollectionComplaint.createPublicQuizCollectionComplaint(content, publicQuizCollection, member);
        publicQuizCollectionComplaintRepository.save(publicQuizCollectionComplaint);

        if (files != null && !files.isEmpty()) {
            createPublicQuizCollectionComplaintFile(files, publicQuizCollectionComplaint);
        }

        return publicQuizCollectionComplaint;
    }

    @Transactional
    private void createPublicQuizCollectionComplaintFile(List<MultipartFile> files, PublicQuizCollectionComplaint publicQuizCollectionComplaint) {

        String customS3Key = UUID.randomUUID().toString();
        String s3FolderPath = "picktoss-collection-complaint-images/";

        List<String> s3Keys = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fullS3Key = s3FolderPath + customS3Key + "_" + fileName;
            s3Keys.add(fullS3Key);
        }

        List<PublicQuizCollectionComplaintFile> publicQuizCollectionComplaintFiles = new ArrayList<>();

        for (String s3Key : s3Keys) {
            PublicQuizCollectionComplaintFile publicQuizCollectionComplaintFile = PublicQuizCollectionComplaintFile.createPublicQuizCollectionComplaintFile(s3Key, publicQuizCollectionComplaint);
            publicQuizCollectionComplaintFiles.add(publicQuizCollectionComplaintFile);
        }

        s3UploadImagesPublisher.s3UploadImagesPublisher(new S3UploadImagesEvent(files, s3Keys));
        publicQuizCollectionComplaintFileRepository.saveAll(publicQuizCollectionComplaintFiles);
    }
}

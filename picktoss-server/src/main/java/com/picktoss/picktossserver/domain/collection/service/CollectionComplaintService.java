package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadImagesEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadImagesPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionComplaint;
import com.picktoss.picktossserver.domain.collection.entity.CollectionComplaintFile;
import com.picktoss.picktossserver.domain.collection.repository.*;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
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
public class CollectionComplaintService {

    private final CollectionRepository collectionRepository;
    private final CollectionQuizRepository collectionQuizRepository;
    private final CollectionBookmarkRepository collectionBookmarkRepository;
    private final CollectionComplaintRepository collectionComplaintRepository;
    private final CollectionComplaintFileRepository collectionComplaintFileRepository;
    private final MemberRepository memberRepository;
    private final S3UploadImagesPublisher s3UploadImagesPublisher;

    @Transactional
    public void createCollectionComplaint(Long collectionId, String content, Long memberId, List<MultipartFile> files) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        String customS3Key = UUID.randomUUID().toString();
        String s3FolderPath = "picktoss-collection-complaint-images/";

        List<String> s3Keys = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fullS3Key = s3FolderPath + customS3Key + "_" + fileName;
            s3Keys.add(fullS3Key);
        }

        s3UploadImagesPublisher.s3UploadImagesPublisher(new S3UploadImagesEvent(files, s3Keys));

        Collection collection = collectionRepository.findCollectionById(collectionId)
                .orElseThrow(() -> new CustomException(ErrorInfo.COLLECTION_NOT_FOUND));
        CollectionComplaint collectionComplaint = CollectionComplaint.createCollectionComplaint(content, collection, member);

        List<CollectionComplaintFile> collectionComplaintFiles = new ArrayList<>();

        for (String s3Key : s3Keys) {
            CollectionComplaintFile collectionComplaintFile = CollectionComplaintFile.createCollectionComplaintFile(s3Key, collectionComplaint);
            collectionComplaintFiles.add(collectionComplaintFile);
        }

        collectionComplaintRepository.save(collectionComplaint);
        collectionComplaintFileRepository.saveAll(collectionComplaintFiles);
    }
}

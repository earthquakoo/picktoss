package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadImagesEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadImagesPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentComplaint;
import com.picktoss.picktossserver.domain.document.entity.DocumentComplaintFile;
import com.picktoss.picktossserver.domain.document.repository.DocumentComplaintFileRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentComplaintRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.global.enums.document.ComplaintReason;
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
public class DocumentComplaintCreateService {

    private final MemberRepository memberRepository;
    private final DocumentRepository documentRepository;
    private final DocumentComplaintRepository documentComplaintRepository;
    private final DocumentComplaintFileRepository documentComplaintFileRepository;
    private final S3UploadImagesPublisher s3UploadImagesPublisher;

    @Transactional
    public DocumentComplaint createDocumentComplaint(Long memberId, Long documentId, String content, ComplaintReason complaintReason, List<MultipartFile> files) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new CustomException(ErrorInfo.DOCUMENT_NOT_FOUND));

        DocumentComplaint documentComplaint = DocumentComplaint.createDocumentComplaint(content, complaintReason, member, document);
        documentComplaintRepository.save(documentComplaint);

        if (files != null && !files.isEmpty()) {
            createDocumentComplaintFiles(files, documentComplaint);
        }

        return documentComplaint;
    }

    @Transactional
    private void createDocumentComplaintFiles(List<MultipartFile> files, DocumentComplaint documentComplaint) {
        String customS3Key = UUID.randomUUID().toString();
        String s3FolderPath = "picktoss-collection-complaint-images/";

        List<String> s3Keys = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fullS3Key = s3FolderPath + customS3Key + "_" + fileName;
            s3Keys.add(fullS3Key);
        }

        List<DocumentComplaintFile> documentComplaintFiles = new ArrayList<>();

        for (String s3Key : s3Keys) {
            DocumentComplaintFile documentComplaintFile = DocumentComplaintFile.createDocumentComplaintFile(s3Key, documentComplaint);
            documentComplaintFiles.add(documentComplaintFile);
        }

        s3UploadImagesPublisher.s3UploadImagesPublisher(new S3UploadImagesEvent(files, s3Keys));
        documentComplaintFileRepository.saveAll(documentComplaintFiles);
    }
}

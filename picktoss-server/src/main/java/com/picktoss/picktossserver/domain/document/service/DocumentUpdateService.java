package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3DeleteEvent;
import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3DeletePublisher;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.repository.CategoryRepository;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.global.enums.document.QuizGenerationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentUpdateService {

    private final DocumentRepository documentRepository;
    private final S3UploadPublisher s3UploadPublisher;
    private final S3DeletePublisher s3DeletePublisher;
    private final CategoryRepository categoryRepository;


    @Transactional
    public void updateDocumentContent(MultipartFile file, Long documentId, Long memberId, String name, String language) {
        String s3Key = UUID.randomUUID().toString();

        Document document = documentRepository.findByDocumentIdAndMemberIdAndLanguage(documentId, memberId, language)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        document.updateDocumentS3KeyByUpdatedContent(s3Key);
        document.updateDocumentName(name);

        s3DeletePublisher.s3DeletePublisher(new S3DeleteEvent(s3Key));
        s3UploadPublisher.s3UploadPublisher(new S3UploadEvent(file, s3Key));
    }

    @Transactional
    public void updateDocumentName(Long documentId, Long memberId, String documentName, String language) {
        Document document = documentRepository.findByDocumentIdAndMemberIdAndLanguage(documentId, memberId, language)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        document.updateDocumentName(documentName);
    }

    @Transactional
    public void updateDocumentCategory(Long documentId, Long memberId, Long categoryId, String language) {
        Document document = documentRepository.findByDocumentIdAndMemberIdAndLanguage(documentId, memberId, language)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        document.updateDocumentCategory(category);
    }

    @Transactional
    public void updateDocumentEmoji(Long documentId, Long memberId, String emoji, String language) {
        Document document = documentRepository.findByDocumentIdAndMemberIdAndLanguage(documentId, memberId, language)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        document.updateDocumentEmoji(emoji);
    }

    @Transactional
    public void updateDocumentIsPublic(Long documentId, Long memberId, Boolean isPublic, String language) {
        Document document = documentRepository.findByDocumentIdAndMemberIdAndLanguage(documentId, memberId, language)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        if (document.getQuizGenerationStatus() == QuizGenerationStatus.QUIZ_GENERATION_ERROR) {
            throw new CustomException(QUIZ_GENERATION_FAILED);
        }

        document.updateDocumentIsPublic(isPublic);
    }
}

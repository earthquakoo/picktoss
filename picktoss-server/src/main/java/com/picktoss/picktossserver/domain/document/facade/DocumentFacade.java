package com.picktoss.picktossserver.domain.document.facade;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3DeleteEvent;
import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadEvent;
import com.picktoss.picktossserver.core.eventlistener.event.sqs.SQSMessageEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3DeletePublisher;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadPublisher;
import com.picktoss.picktossserver.core.eventlistener.publisher.sqs.SQSEventMessagePublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.directory.service.DirectoryService;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.service.CollectionService;
import com.picktoss.picktossserver.domain.document.controller.response.*;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.outbox.service.OutboxService;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.service.StarService;
import com.picktoss.picktossserver.global.enums.document.DocumentSortOption;
import com.picktoss.picktossserver.global.enums.document.DocumentType;
import com.picktoss.picktossserver.global.enums.outbox.OutboxStatus;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.MAX_POSSESS_DOCUMENT_COUNT;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentFacade {

    private final DocumentService documentService;
    private final DirectoryService directoryService;
    private final OutboxService outboxService;
    private final CollectionService collectionService;
    private final StarService starService;
    private final QuizService quizService;

    private final SQSEventMessagePublisher sqsEventMessagePublisher;
    private final S3UploadPublisher s3UploadPublisher;
    private final S3DeletePublisher s3DeletePublisher;

    @Transactional
    public Long createDocument(
            String documentName, MultipartFile file, Long memberId, Long directoryId, Integer starCount, QuizType quizType, DocumentType documentType) {
        List<Document> documents = documentService.findAllByMemberId(memberId);
        int possessDocumentCount = documents.size();

        if (possessDocumentCount >= MAX_POSSESS_DOCUMENT_COUNT) {
            throw new CustomException(DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
        }

        Directory directory = directoryService.findDirectoryWithMemberAndStarAndStarHistoryByDirectoryIdAndMemberId(directoryId, memberId);
        Star star = directory.getMember().getStar();
        starService.withdrawalStarByCreateDocument(star, starCount);

        String s3Key = UUID.randomUUID().toString();
        Document document = documentService.createDocument(documentName, s3Key, documentType, directory);

        outboxService.createOutbox(OutboxStatus.WAITING, quizType, starCount, document);
        s3UploadPublisher.s3UploadPublisher(new S3UploadEvent(file, s3Key));
        sqsEventMessagePublisher.sqsEventMessagePublisher(new SQSMessageEvent(memberId, s3Key, document.getId(), quizType, starCount));
        return document.getId();
    }

    public GetSingleDocumentResponse findSingleDocument(Long memberId, Long documentId) {
        return documentService.findSingleDocument(memberId, documentId);
    }

    public List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> findAllDocumentsInDirectory(Long memberId, Long directoryId, DocumentSortOption documentSortOption) {
        List<QuizSetQuiz> quizSetQuizzes = quizService.findQuizSetQuizzesByMemberIdAndCreatedAtAfterSevenDaysAgo(memberId);
        return documentService.findAllDocumentsInDirectory(memberId, directoryId, documentSortOption, quizSetQuizzes);
    }

    @Transactional
    public void deleteDocument(Long memberId, List<Long> documentIds) {
        List<Document> documents = documentService.deleteDocument(memberId, documentIds);
        for (Document document : documents) {
            s3DeletePublisher.s3DeletePublisher(new S3DeleteEvent(document.getS3Key()));
        }
    }

    @Transactional
    public void moveDocumentToDirectory(List<Long> documentIds, Long memberId, Long directoryId) {
        Directory directory = directoryService.findByDirectoryIdAndMemberId(directoryId, memberId);
        documentService.moveDocumentToDirectory(documentIds, memberId, directory);
    }

    public SearchDocumentResponse searchDocumentByKeyword(String keyword, Long memberId) {
        return documentService.searchDocumentByKeyword(keyword, memberId);
    }

    public GetDocumentsNeedingReviewResponse findDocumentsNeedingReview(Long memberId) {
        List<QuizSetQuiz> quizSetQuizzes = quizService.findQuizSetQuizzesByMemberIdAndCreatedAtAfterSevenDaysAgo(memberId);
        return documentService.findDocumentsNeedingReview(memberId, quizSetQuizzes);
    }

    @Transactional
    public void updateDocumentContent(Long documentId, Long memberId, String name, MultipartFile file) {
        String s3Key = UUID.randomUUID().toString();
        documentService.updateDocumentContent(documentId, memberId, name, s3Key);
        s3DeletePublisher.s3DeletePublisher(new S3DeleteEvent(s3Key));
        s3UploadPublisher.s3UploadPublisher(new S3UploadEvent(file, s3Key));
    }

    @Transactional
    public void updateDocumentName(Long documentId, Long memberId, String documentName) {
        documentService.updateDocumentName(documentId, memberId, documentName);
    }

    @Transactional
    public void selectDocumentToNotGenerateByTodayQuiz(Map<Long, Boolean> documentIdTodayQuizMap, Long memberId) {
        documentService.selectDocumentToNotGenerateByTodayQuiz(documentIdTodayQuizMap, memberId);
    }

    public IntegratedSearchResponse integratedSearchByKeyword(Long memberId, String keyword) {
        List<Collection> collections = collectionService.searchCollections(keyword);
        return documentService.integratedSearchByKeyword(memberId, keyword, collections);
    }
}

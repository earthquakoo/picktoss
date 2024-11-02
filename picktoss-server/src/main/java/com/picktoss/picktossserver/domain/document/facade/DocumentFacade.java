package com.picktoss.picktossserver.domain.document.facade;

import com.picktoss.picktossserver.core.event.event.s3.S3DeleteEvent;
import com.picktoss.picktossserver.core.event.event.s3.S3UploadEvent;
import com.picktoss.picktossserver.core.event.event.sqs.SQSMessageEvent;
import com.picktoss.picktossserver.core.event.publisher.s3.S3DeletePublisher;
import com.picktoss.picktossserver.core.event.publisher.s3.S3UploadPublisher;
import com.picktoss.picktossserver.core.event.publisher.sqs.SQSEventMessagePublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
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
import com.picktoss.picktossserver.global.enums.outbox.OutboxStatus;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.FREE_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.MAX_POSSESS_DOCUMENT_COUNT;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentFacade {

    private final DocumentService documentService;
    private final CategoryService categoryService;
    private final OutboxService outboxService;
    private final CollectionService collectionService;
    private final StarService starService;
    private final QuizService quizService;

    private final SQSEventMessagePublisher sqsEventMessagePublisher;
    private final S3UploadPublisher s3UploadPublisher;
    private final S3DeletePublisher s3DeletePublisher;

    @Transactional
    public Long createDocument(
            String documentName, MultipartFile file, Long memberId, Long categoryId, Integer starCount, QuizType quizType) {
        List<Document> documents = documentService.findAllByMemberId(memberId);
        int possessDocumentCount = documents.size();

        if (possessDocumentCount >= MAX_POSSESS_DOCUMENT_COUNT) {
            throw new CustomException(FREE_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
        }

        Category category = categoryService.findCategoryWithMemberAndStarAndStarHistoryByCategoryIdAndMemberId(categoryId, memberId);
        Star star = category.getMember().getStar();
        starService.withdrawalStarByCreateDocument(star, starCount);

        String s3Key = UUID.randomUUID().toString();
        Document document = documentService.createDocument(documentName, category, memberId, s3Key, starCount);

        outboxService.createOutbox(OutboxStatus.WAITING, document);
        s3UploadPublisher.s3UploadPublisher(new S3UploadEvent(file, s3Key));
        sqsEventMessagePublisher.sqsEventMessagePublisher(new SQSMessageEvent(memberId, s3Key, document.getId(), quizType, starCount));
        return document.getId();
    }

    public GetSingleDocumentResponse findSingleDocument(Long memberId, Long documentId) {

        return documentService.findSingleDocument(memberId, documentId);
    }

    public List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> findAllDocumentsInCategory(Long memberId, Long categoryId, DocumentSortOption documentSortOption) {
        List<QuizSetQuiz> quizSetQuizzes = quizService.findQuizSetQuizzesByMemberIdAndCreatedAtAfter(memberId);
        return documentService.findAllDocumentsInCategory(memberId, categoryId, documentSortOption, quizSetQuizzes);
    }

    @Transactional
    public void deleteDocument(Long memberId, List<Long> documentIds) {
        List<Document> documents = documentService.deleteDocument(memberId, documentIds);
        for (Document document : documents) {
            s3DeletePublisher.s3DeletePublisher(new S3DeleteEvent(document.getS3Key()));
        }
    }

    @Transactional
    public void moveDocumentToCategory(List<Long> documentIds, Long memberId, Long categoryId) {
        Category category = categoryService.findByCategoryIdAndMemberId(categoryId, memberId);
        documentService.moveDocumentToCategory(documentIds, memberId, category);
    }

    public SearchDocumentResponse searchDocumentByKeyword(String keyword, Long memberId) {
        return documentService.searchDocumentByKeyword(keyword, memberId);
    }

    public GetDocumentsNeedingReviewResponse findDocumentsNeedingReview(Long memberId) {
        List<QuizSetQuiz> quizSetQuizzes = quizService.findQuizSetQuizzesByMemberIdAndCreatedAtAfter(memberId);
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

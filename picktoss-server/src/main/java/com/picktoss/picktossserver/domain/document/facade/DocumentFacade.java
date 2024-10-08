package com.picktoss.picktossserver.domain.document.facade;

import com.picktoss.picktossserver.core.event.event.S3Event;
import com.picktoss.picktossserver.core.event.event.SQSEvent;
import com.picktoss.picktossserver.core.event.publisher.S3UploadPublisher;
import com.picktoss.picktossserver.core.event.publisher.SQSEventMessagePublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import com.picktoss.picktossserver.domain.document.controller.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetMostIncorrectDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.controller.response.SearchDocumentResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.keypoint.service.KeyPointService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.outbox.service.OutboxService;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import com.picktoss.picktossserver.global.enums.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.FREE_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.FREE_PLAN_MAX_POSSESS_DOCUMENT_COUNT;
import static com.picktoss.picktossserver.global.enums.DocumentStatus.DEFAULT_DOCUMENT;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentFacade {

    private final DocumentService documentService;
    private final CategoryService categoryService;
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final QuizService quizService;
    private final KeyPointService keyPointService;
    private final OutboxService outboxService;

    private final SQSEventMessagePublisher sqsEventMessagePublisher;
    private final S3UploadPublisher s3UploadPublisher;

    @Transactional
    public Long createDocument(String documentName, MultipartFile file, Long memberId, Long categoryId) {
        List<Document> documents = documentService.findAllByMemberId(memberId);
        int possessDocumentCount = documents.size();
        for (Document document : documents) {
            if (document.getStatus() == DEFAULT_DOCUMENT) {
                possessDocumentCount -= 1;
            }
        }

        String s3Key = UUID.randomUUID().toString();

        if (possessDocumentCount >= FREE_PLAN_MAX_POSSESS_DOCUMENT_COUNT) {
            throw new CustomException(FREE_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
        }

        Category category = categoryService.findByCategoryIdAndMemberId(categoryId, memberId);
//        Set<Document> documentsByCategory = category.getDocuments();
//        int lastOrder = 1;
//        for (Document document : documentsByCategory) {
//            if (lastOrder < document.getOrder()) {
//                lastOrder = document.getOrder();
//            }
//        }
//        lastOrder += 1;

        s3UploadPublisher.s3UploadPublisher(new S3Event(file, s3Key));
        return documentService.createDocument(documentName, category, memberId, s3Key);
    }

    @Transactional
    public boolean createAiPick(Long documentId, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);
        Document document = documentService.findByDocumentIdAndMemberId(documentId, memberId);
        boolean isFirstAiPick = documentService.createAiPick(document, memberId, subscription, member);
        outboxService.createOutbox(OutboxStatus.WAITING, document);
        sqsEventMessagePublisher.sqsEventMessagePublisher(new SQSEvent(memberId, document.getS3Key(), documentId, subscription.getSubscriptionPlanType()));
        return isFirstAiPick;
    }

    public GetSingleDocumentResponse findSingleDocument(Long memberId, Long documentId) {
        return documentService.findSingleDocument(memberId, documentId);
    }

    public List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> findAllDocuments(Long memberId, Long categoryId, String documentSortOption) {
        return documentService.findAllDocuments(memberId, categoryId, documentSortOption);
    }

    @Transactional
    public void deleteDocument(Long memberId, Long documentId) {
        documentService.deleteDocument(memberId, documentId);
    }

    @Transactional
    public void changeDocumentOrder(Long documentId, int preDragDocumentOrder, int afterDragDocumentOrder, Long memberId) {
        documentService.changeDocumentOrder(documentId, preDragDocumentOrder, afterDragDocumentOrder, memberId);
    }

    @Transactional
    public void moveDocumentToCategory(Long documentId, Long memberId, Long categoryId) {
        Category category = categoryService.findByCategoryIdAndMemberId(categoryId, memberId);
        documentService.moveDocumentToCategory(documentId, memberId, category);
    }

    public List<SearchDocumentResponse.SearchDocumentDto> searchDocument(String word, Long memberId) {
        return documentService.searchDocument(word, memberId);
    }

    public GetMostIncorrectDocumentsResponse findMostIncorrectDocuments(Long memberId) {
        return documentService.findMostIncorrectDocuments(memberId);
    }

    @Transactional
    public void updateDocumentContent(Long documentId, Long memberId, String name, MultipartFile file) {
        String s3Key = UUID.randomUUID().toString();
        documentService.updateDocumentContent(documentId, memberId, name, s3Key);
        s3UploadPublisher.s3UploadPublisher(new S3Event(file, s3Key));
    }

    @Transactional
    public void updateDocumentName(Long documentId, Long memberId, String documentName) {
        documentService.updateDocumentName(documentId, memberId, documentName);
    }

    @Transactional
    public void reUploadDocument(Long documentId, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);
        Document document = documentService.findByDocumentIdAndMemberId(documentId, memberId);
        quizService.updateQuizLatest(document);
        keyPointService.deleteKeyPointByDocumentReUpload(document);
        documentService.reUploadDocument(document, subscription, member);
        document.updateDocumentStatusProcessingByGenerateAiPick();
    }

    @Transactional
    public void selectDocumentToNotGenerateByTodayQuiz(List<Long> documentIds, Long memberId) {
        documentService.selectDocumentToNotGenerateByTodayQuiz(documentIds, memberId);
    }
}

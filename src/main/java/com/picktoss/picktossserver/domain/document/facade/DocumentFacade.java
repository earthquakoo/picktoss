package com.picktoss.picktossserver.domain.document.facade;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import com.picktoss.picktossserver.domain.document.controller.response.*;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.keypoint.service.KeyPointService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.FREE_PLAN_AI_PICK_LIMIT_EXCEED_ERROR;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.FREE_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.*;

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

    @Transactional
    public Long createDocument(String documentName, MultipartFile file, Long memberId, Long categoryId) {
        int possessDocumentCount = documentService.findPossessDocumentCount(memberId);

        if (possessDocumentCount >= FREE_PLAN_MAX_POSSESS_DOCUMENT_COUNT) {
            throw new CustomException(FREE_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
        }

        Category category = categoryService.findByCategoryIdAndMemberId(categoryId, memberId);
        return documentService.createDocument(documentName, file, category, memberId);
    }

    @Transactional
    public boolean createAiPick(Long documentId, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);
        return documentService.createAiPick(documentId, memberId, subscription, member);
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

    public GetQuizCountByDocumentResponse findQuizCountByDocument(List<Long> documentIds, Long memberId) {
        return documentService.findQuizCountByDocument(documentIds, memberId);
    }

    @Transactional
    public void updateDocumentContent(Long documentId, Long memberId, String name, MultipartFile file) {
        documentService.updateDocumentContent(documentId, memberId, name, file);
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
}

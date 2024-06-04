package com.picktoss.picktossserver.domain.document.facade;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import com.picktoss.picktossserver.domain.document.controller.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetMostIncorrectDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.controller.response.SearchDocumentResponse;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
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
    public void createAiPick(Long documentId, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);

        int aiPickCount = member.getAiPickCount();
        int availableAiPickCount = FREE_PLAN_DEFAULT_DOCUMENT_COUNT + subscription.getAvailableAiPickCount() - aiPickCount;

        if (availableAiPickCount >= AVAILABLE_AI_PICK_COUNT) {
            if (subscription.getAvailableAiPickCount() < 1) {
                throw new CustomException(FREE_PLAN_AI_PICK_LIMIT_EXCEED_ERROR);
            }
            subscription.minusAvailableAiPickCount();
        }

        documentService.createAiPick(documentId, memberId, subscription);
        member.useAiPick();
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
        quizService.updateQuizLatest(documentId);
        documentService.reUploadDocument(documentId, memberId, subscription);
    }
}

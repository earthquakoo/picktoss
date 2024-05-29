package com.picktoss.picktossserver.domain.document.facade;

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

import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.FREE_PLAN_DEFAULT_DOCUMENT_COUNT;

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
        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);

        int possessDocumentCount = findPossessDocumentCount(memberId);
        int uploadedDocumentCount = findUploadedDocumentCount(memberId);

        int uploadableDocumentCount = FREE_PLAN_DEFAULT_DOCUMENT_COUNT + subscription.getUploadedDocumentCount() - uploadedDocumentCount;

        subscriptionService.checkDocumentUploadLimit(subscription, possessDocumentCount, uploadedDocumentCount, uploadableDocumentCount);

        if (uploadedDocumentCount >= FREE_PLAN_DEFAULT_DOCUMENT_COUNT) {
            subscription.minusUploadedDocumentCount();
        }

        Category category = categoryService.findByCategoryIdAndMemberId(categoryId, memberId);
        Long documentId = documentService.createDocument(documentName, file, subscription, category, memberId);
        System.out.println("possessDocumentCount = " + possessDocumentCount);
        if (possessDocumentCount == 0) {
            quizService.createInitialQuizSet(documentId, member);
        }
        return documentId;
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

    public int findPossessDocumentCount(Long memberId) {
        return documentService.findPossessDocumentCount(memberId);
    }

    public int findUploadedDocumentCountForCurrentSubscription(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);
        return documentService.findUploadedDocumentCountForCurrentSubscription(memberId, subscription);
    }

    public int findUploadedDocumentCount(Long memberId) {
        return documentService.findUploadedDocumentCount(memberId);
    }
}

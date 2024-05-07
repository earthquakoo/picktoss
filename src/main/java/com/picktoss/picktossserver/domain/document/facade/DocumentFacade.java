package com.picktoss.picktossserver.domain.document.facade;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import com.picktoss.picktossserver.domain.document.controller.request.ChangeDocumentsOrderRequest;
import com.picktoss.picktossserver.domain.document.controller.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetMostIncorrectDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.controller.response.SearchDocumentNameResponse;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentFacade {

    private final DocumentService documentService;
    private final CategoryService categoryService;
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;

    @Transactional
    public Long saveDocument(String documentName, DocumentStatus documentStatus, MultipartFile file, Long memberId, Long categoryId) {
        int possessDocumentCount = findPossessDocumentCount(memberId);
        int uploadedDocumentCount = findUploadedDocumentCount(memberId);
        int uploadedDocumentCountForCurrentSubscription = findUploadedDocumentCountForCurrentSubscription(memberId);

        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);
        subscriptionService.checkDocumentUploadLimit(subscription, possessDocumentCount, uploadedDocumentCount, uploadedDocumentCountForCurrentSubscription);

        Category category = categoryService.findByCategoryIdAndMemberId(categoryId, memberId);
        return documentService.saveDocument(documentName, documentStatus, file, subscription, category, member, memberId);
    }

    public GetSingleDocumentResponse findSingleDocument(Long memberId, Long documentId) {
        return documentService.findSingleDocument(memberId, documentId);
    }

    public List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> findAllDocuments(Long memberId, Long categoryId) {
        return documentService.findAllDocuments(memberId, categoryId);
    }

    @Transactional
    public void deleteDocument(Long memberId, Long documentId) {
        documentService.deleteDocument(memberId, documentId);
    }

    @Transactional
    public void changeDocumentOrder(List<ChangeDocumentsOrderRequest.ChangeDocumentDto> documentDtos, Long memberId) {
        documentService.changeDocumentOrder(documentDtos, memberId);
    }

    @Transactional
    public void moveDocumentToCategory(Long documentId, Long memberId, Long categoryId) {
        Category category = categoryService.findByCategoryIdAndMemberId(categoryId, memberId);
        documentService.moveDocumentToCategory(documentId, memberId, category);
    }

    public SearchDocumentNameResponse searchDocumentName(String word, Long memberId) {
        return documentService.searchDocumentName(word, memberId);
    }

    public GetMostIncorrectDocumentsResponse findMostIncorrectDocuments(Long memberId) {
        return documentService.findMostIncorrectDocuments(memberId);
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

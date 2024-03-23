package com.picktoss.picktossserver.domain.document.facade;

import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import com.picktoss.picktossserver.domain.document.controller.response.CreateDocumentResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import com.picktoss.picktossserver.global.enums.DocumentFormat;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Long saveDocument(String documentName, String s3Key, String memberId, Long categoryId) {
        int numCurrentUploadDocument = findNumCurrentUploadDocument(memberId);
        int numUploadedDocumentsForCurrentSubscription = findNumUploadedDocumentsForCurrentSubscription(memberId);

        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);
                subscriptionService.checkDocumentUploadLimit(subscription, numCurrentUploadDocument, numUploadedDocumentsForCurrentSubscription);


        Category category = categoryService.findCategoryByMemberAndCategoryId(member, categoryId);
        return documentService.saveDocument(documentName, s3Key, subscription, category, member);
    }

    public List<GetSingleDocumentResponse.DocumentDto> findSingleDocument(String memberId, Long documentId) {
        Member member = memberService.findMemberById(memberId);
        Category category = categoryService.findCategoryByMember(member);
        return documentService.findSingleDocument(documentId, category);
    }

    public List<GetAllDocumentsResponse.DocumentDto> findAllDocuments(Long categoryId) {
        return documentService.findAllDocuments(categoryId);
    }

    public Document findDocumentByCategoryAndDocumentId(Category category, Long documentId) {
        return documentService.findDocumentByCategoryAndDocumentId(category, documentId);
    }

    public int findNumCurrentUploadDocument(String memberId) {
        return documentService.findNumCurrentUploadDocument(memberId);
    }

    public int findNumUploadedDocumentsForCurrentSubscription(String memberId) {
        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);
        return documentService.findNumUploadedDocumentsForCurrentSubscription(memberId, subscription);
    }


}

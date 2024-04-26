package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.core.sqs.SqsProvider;
import com.picktoss.picktossserver.domain.category.controller.request.UpdateCategoriesOrderRequest;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.controller.request.UpdateDocumentsOrderRequest;
import com.picktoss.picktossserver.domain.document.controller.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentUpload;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentUploadRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.question.entity.Question;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.UNAUTHORIZED_OPERATION_EXCEPTION;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final S3Provider s3Provider;
    private final SqsProvider sqsProvider;

    private final DocumentRepository documentRepository;
    private final DocumentUploadRepository documentUploadRepository;

    @Transactional
    public Long saveDocument(String documentName, String s3Key, Subscription subscription, Category category, Member member, Long memberId) {
        Integer lastOrder = documentRepository.findLastOrderByMemberId(memberId);
        if (lastOrder == null) {
            lastOrder = 0;
        }

        int order = lastOrder;

        Document document = Document.createDocument(documentName, s3Key, order + 1, DocumentStatus.UNPROCESSED, true, category);

        DocumentUpload documentUpload = DocumentUpload.builder()
                .member(member)
                .build();

        documentRepository.save(document);
        documentUploadRepository.save(documentUpload);

        sqsProvider.sendMessage(s3Key, document.getId(), subscription.getSubscriptionPlanType());

        return document.getId();
    }

    public GetSingleDocumentResponse findSingleDocument(Long memberId, Long documentId) {
        Document document = documentRepository.findByDocumentIdAndMemberId(documentId, memberId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        List<Question> questions = document.getQuestions();
        List<GetSingleDocumentResponse.GetSingleDocumentKeyPointDto> questionDtos = new ArrayList<>();

        String content = s3Provider.findFile(document.getS3Key());

        for (Question question : questions) {
            GetSingleDocumentResponse.GetSingleDocumentKeyPointDto questionDto = GetSingleDocumentResponse.GetSingleDocumentKeyPointDto.builder()
                    .id(question.getId())
                    .question(question.getQuestion())
                    .answer(question.getAnswer())
                    .build();

            questionDtos.add(questionDto);
        }

        GetSingleDocumentResponse.GetSingleDocumentCategoryDto categoryDto = GetSingleDocumentResponse.GetSingleDocumentCategoryDto.builder()
                .id(document.getCategory().getId())
                .name(document.getCategory().getName())
                .build();

        return GetSingleDocumentResponse.builder()
                .id(document.getId())
                .documentName(document.getName())
                .status(document.getStatus())
                .quizGenerationStatus(true)
                .category(categoryDto)
                .keyPoints(questionDtos)
                .content(content)
                .createdAt(document.getCreatedAt())
                .build();
    }

    public List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> findAllDocuments(Long memberId, Long categoryId) {
        List<Document> documents = documentRepository.findAllByCategoryIdAndMemberId(categoryId, memberId);
        List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            DocumentStatus status = DocumentStatus.UNPROCESSED;
            if (document.getStatus() == DocumentStatus.PARTIAL_SUCCESS ||
                    document.getStatus() == DocumentStatus.PROCESSED ||
                    document.getStatus() == DocumentStatus.COMPLETELY_FAILED) {
                status = DocumentStatus.PROCESSED;
            }

            GetAllDocumentsResponse.GetAllDocumentsDocumentDto documentDto = GetAllDocumentsResponse.GetAllDocumentsDocumentDto.builder()
                    .id(document.getId())
                    .documentName(document.getName())
                    .status(status)
                    .quizGenerationStatus(true)
                    .createdAt(document.getCreatedAt())
                    .build();

            documentDtos.add(documentDto);
        }
        return documentDtos;
    }

    @Transactional
    public void deleteDocument(Long memberId, Long documentId) {
        Optional<Document> optionalDocument = documentRepository.findById(documentId);
        if (optionalDocument.isEmpty()) {
            return ;
        }

        Document document = optionalDocument.get();
        if (!Objects.equals(document.getCategory().getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }

        documentRepository.delete(document);
    }

    @Transactional
    public void updateDocumentOrder(List<UpdateDocumentsOrderRequest.UpdateDocumentDto> documentDtos, Long memberId) {
        for (UpdateDocumentsOrderRequest.UpdateDocumentDto documentDto : documentDtos) {
            Optional<Document> optionalDocument = documentRepository.findByDocumentIdAndMemberId(documentDto.getId(), memberId);

            if (optionalDocument.isEmpty()) {
                return ;
            }

            Document document = optionalDocument.get();

            document.updateDocumentOrder(documentDto.getOrder());
        }
    }

    //현재 시점에 업로드된 문서 개수
    public int findNumCurrentUploadDocument(Long memberId) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        return documents.size();
    }

    //현재 구독 사이클에 업로드한 문서 개수
    public int findNumUploadedDocumentsForCurrentSubscription(Long memberId, Subscription subscription) {
        List<DocumentUpload> uploadedDocuments = documentUploadRepository.findAllByMemberId(memberId);

        List<DocumentUpload> currentSubscriptionDocumentUploads = new ArrayList<>();
        for (DocumentUpload doc : uploadedDocuments) {
            if ((doc.getCreatedAt().isAfter(subscription.getPurchasedDate()) ||
                    doc.getCreatedAt().isEqual(subscription.getPurchasedDate())) &&
                    doc.getCreatedAt().isBefore(subscription.getExpireDate())) {
                currentSubscriptionDocumentUploads.add(doc);
            }
        }
        return currentSubscriptionDocumentUploads.size();
    }

    public List<Document> findAllByCategoryIdAndMemberId(Long categoryId, Long memberId) {
        return documentRepository.findAllByCategoryIdAndMemberId(categoryId, memberId);
    }

    public List<Document> findAllByMemberId(Long memberId) {
        return documentRepository.findAllByMemberId(memberId);
    }
}

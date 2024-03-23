package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.core.sqs.SqsProvider;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import com.picktoss.picktossserver.domain.document.controller.response.CreateDocumentResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentUpload;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentUploadRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.question.entity.Question;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import com.picktoss.picktossserver.global.enums.DocumentFormat;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final S3Provider s3Provider;
    private final SqsProvider sqsProvider;

    private final DocumentRepository documentRepository;
    private final DocumentUploadRepository documentUploadRepository;


    @Transactional
    public Long saveDocument(String documentName, String s3Key, Subscription subscription, Category category, Member member) {
        Document document = Document.builder()
                .name(documentName)
                .s3Key(s3Key)
                .format(DocumentFormat.MARKDOWN)
                .status(DocumentStatus.UNPROCESSED)
                .category(category)
                .build();

        DocumentUpload documentUpload = DocumentUpload.builder()
                .document(document)
                .member(member)
                .build();

        documentRepository.save(document);
        documentUploadRepository.save(documentUpload);

        sqsProvider.sendMessage(s3Key, document.getId(), subscription.getSubscriptionPlanType());

        return document.getId();
    }


    public List<GetSingleDocumentResponse.DocumentDto> findSingleDocument(Long documentId, Category category) {
        Document document = findDocumentByCategoryAndDocumentId(category, documentId);
        List<Question> questions = document.getQuestions();
        List<GetSingleDocumentResponse.QuestionDto> questionDtos = new ArrayList<>();

        String content = s3Provider.findFile(document.getS3Key());

        for (Question question : questions) {
            GetSingleDocumentResponse.QuestionDto questionDto = GetSingleDocumentResponse.QuestionDto.builder()
                    .id(question.getId())
                    .question(question.getQuestion())
                    .answer(question.getAnswer())
                    .build();

            questionDtos.add(questionDto);
        }

        GetSingleDocumentResponse.CategoryDto categoryDto = GetSingleDocumentResponse.CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();

        List<GetSingleDocumentResponse.DocumentDto> documentDtos = new ArrayList<>();
        GetSingleDocumentResponse.DocumentDto documentDto = GetSingleDocumentResponse.DocumentDto.builder()
                .documentId(document.getId())
                .documentName(document.getName())
                .summary(document.getSummary())
                .status(document.getStatus())
                .format(document.getFormat())
                .categoryDto(categoryDto)
                .questionDtos(questionDtos)
                .content(content)
                .build();

        documentDtos.add(documentDto);

        return documentDtos;
    }

    public List<GetAllDocumentsResponse.DocumentDto> findAllDocuments(Long categoryId) {
        List<Document> documents = documentRepository.findAllByCategoryId(categoryId);
        List<GetAllDocumentsResponse.DocumentDto> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            DocumentStatus status = DocumentStatus.UNPROCESSED;
            if (document.getStatus() == DocumentStatus.PARTIAL_SUCCESS ||
                    document.getStatus() == DocumentStatus.PROCESSED ||
                    document.getStatus() == DocumentStatus.COMPLETELY_FAILED) {
                status = DocumentStatus.PROCESSED;
            }

            GetAllDocumentsResponse.DocumentDto documentDto = GetAllDocumentsResponse.DocumentDto.builder()
                    .documentId(document.getId())
                    .documentName(document.getName())
                    .status(status)
                    .summary(document.getSummary())
                    .build();

            documentDtos.add(documentDto);
        }
        return documentDtos;
    }

    public Document findDocumentByCategoryAndDocumentId(Category category, Long documentId) {
        return documentRepository.findByCategoryAndId(category, documentId)
                .orElseThrow(() -> new CustomException(ErrorInfo.DOCUMENT_NOT_FOUND));
    }

    //현재 시점에 업로드된 문서 개수
    public int findNumCurrentUploadDocument(String memberId) {
        List<Document> uploadedDocuments = documentUploadRepository.findAllByMemberId(memberId);
        return uploadedDocuments.size();
    }

    //현재 구독 사이클에 업로드한 문서 개수
    public int findNumUploadedDocumentsForCurrentSubscription(String memberId, Subscription subscription) {
        List<Document> uploadedDocuments = documentUploadRepository.findAllByMemberId(memberId);

        List<Document> currentSubscriptionDocumentUploads = new ArrayList<>();
        for (Document doc : uploadedDocuments) {
            if ((doc.getCreatedAt().isAfter(subscription.getPurchasedDate()) ||
                    doc.getCreatedAt().isEqual(subscription.getPurchasedDate())) &&
                    doc.getCreatedAt().isBefore(subscription.getExpireDate())) {
                currentSubscriptionDocumentUploads.add(doc);
            }
        }
        return currentSubscriptionDocumentUploads.size();
    }

    public List<Document> findAllByCategoryId(Long categoryId) {
        return documentRepository.findAllByCategoryId(categoryId);
    }

    public List<Document> findAllByMemberId(String memberId) {
        return documentUploadRepository.findAllByMemberId(memberId);
    }

}

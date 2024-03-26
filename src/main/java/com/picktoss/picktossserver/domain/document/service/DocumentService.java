package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.core.sqs.SqsProvider;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.controller.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentUpload;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentUploadRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.question.entity.Question;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.enums.DocumentFormat;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;

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

    public GetSingleDocumentResponse.DocumentDto findSingleDocument(Long memberId, Long categoryId, Long documentId) {
        Document document = documentRepository.findByDocumentIdAndCategoryIdAndMemberId(documentId, categoryId, memberId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

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
                .id(document.getCategory().getId())
                .name(document.getCategory().getName())
                .build();

        GetSingleDocumentResponse.DocumentDto documentDto = GetSingleDocumentResponse.DocumentDto.builder()
                .id(document.getId())
                .documentName(document.getName())
                .summary(document.getSummary())
                .status(document.getStatus())
                .format(document.getFormat())
                .category(categoryDto)
                .questions(questionDtos)
                .content(content)
                .build();

        return documentDto;
    }

    public List<GetAllDocumentsResponse.DocumentDto> findAllDocuments(Long memberId, Long categoryId) {
        List<Document> documents = documentRepository.findAllByCategoryIdAndMemberId(categoryId, memberId);
        List<GetAllDocumentsResponse.DocumentDto> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            DocumentStatus status = DocumentStatus.UNPROCESSED;
            if (document.getStatus() == DocumentStatus.PARTIAL_SUCCESS ||
                    document.getStatus() == DocumentStatus.PROCESSED ||
                    document.getStatus() == DocumentStatus.COMPLETELY_FAILED) {
                status = DocumentStatus.PROCESSED;
            }

            GetAllDocumentsResponse.DocumentDto documentDto = GetAllDocumentsResponse.DocumentDto.builder()
                    .id(document.getId())
                    .documentName(document.getName())
                    .status(status)
                    .summary(document.getSummary())
                    .build();

            documentDtos.add(documentDto);
        }
        return documentDtos;
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

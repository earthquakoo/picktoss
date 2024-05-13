package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.core.sqs.SqsProvider;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.controller.request.ChangeDocumentsOrderRequest;
import com.picktoss.picktossserver.domain.document.controller.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetMostIncorrectDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.controller.response.SearchDocumentNameResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentUpload;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentUploadRepository;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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
    public Long saveDocument(String documentName, MultipartFile file, Subscription subscription, Category category, Member member, Long memberId) {
        String s3Key = s3Provider.uploadFile(file);

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

        List<KeyPoint> keyPoints = document.getKeyPoints();
        List<GetSingleDocumentResponse.GetSingleDocumentKeyPointDto> keyPointDtos = new ArrayList<>();

        String content = s3Provider.findFile(document.getS3Key());

        for (KeyPoint keyPoint : keyPoints) {
            GetSingleDocumentResponse.GetSingleDocumentKeyPointDto keyPointDto = GetSingleDocumentResponse.GetSingleDocumentKeyPointDto.builder()
                    .id(keyPoint.getId())
                    .question(keyPoint.getQuestion())
                    .answer(keyPoint.getAnswer())
                    .build();

            keyPointDtos.add(keyPointDto);
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
                .keyPoints(keyPointDtos)
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
    public void changeDocumentOrder(List<ChangeDocumentsOrderRequest.ChangeDocumentDto> documentDtos, Long memberId) {
        for (ChangeDocumentsOrderRequest.ChangeDocumentDto documentDto : documentDtos) {
            Optional<Document> optionalDocument = documentRepository.findByDocumentIdAndMemberId(documentDto.getId(), memberId);

            if (optionalDocument.isEmpty()) {
                return ;
            }

            Document document = optionalDocument.get();

            document.changeDocumentOrder(documentDto.getOrder());
        }
    }

    @Transactional
    public void moveDocumentToCategory(Long documentId, Long memberId, Category category) {
        Optional<Document> optionalDocument = documentRepository.findByDocumentIdAndMemberId(documentId, memberId);

        if (optionalDocument.isEmpty()) {
            throw new CustomException(DOCUMENT_NOT_FOUND);
        }

        Document document = optionalDocument.get();

        document.moveDocumentToCategory(category);
    }

    public SearchDocumentNameResponse searchDocumentName(String word, Long memberId) {
        Optional<Document> optionalDocument = documentRepository.findBySpecificWord(memberId, word);

        if (optionalDocument.isEmpty()) {
            throw new CustomException(DOCUMENT_NOT_FOUND);
        }

        Document document = optionalDocument.get();

        return SearchDocumentNameResponse.builder()
                .id(document.getId())
                .name(document.getName())
                .status(document.getStatus())
                .quizGenerationStatus(document.isQuizGenerationStatus())
                .createdAt(document.getCreatedAt())
                .build();
    }

    public GetMostIncorrectDocumentsResponse findMostIncorrectDocuments(Long memberId) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);

        HashMap<Long, Integer> documentIncorrectAnswerCounts = new HashMap<>();

        for (Document document : documents) {
            List<Quiz> quizzes = document.getQuizzes();
            int totalIncorrectAnswerCount = 0;

            for (Quiz quiz : quizzes) {
                totalIncorrectAnswerCount += quiz.getIncorrectAnswerCount();
            }

            documentIncorrectAnswerCounts.put(document.getId(), totalIncorrectAnswerCount);
        }

        List<Long> top5Documents = documentIncorrectAnswerCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(5)
                .toList();

        List<GetMostIncorrectDocumentsResponse.GetMostIncorrectDocumentsDto> documentsDtos = new ArrayList<>();
        for (Long documentId : top5Documents) {
            Optional<Document> optionalDocument = documentRepository.findById(documentId);
            if (optionalDocument.isEmpty()) {
                throw new CustomException(DOCUMENT_NOT_FOUND);
            }
            Document document = optionalDocument.get();
            Category category = document.getCategory();


            GetMostIncorrectDocumentsResponse.GetMostIncorrectDocumentsCategoryDto categoryDto = GetMostIncorrectDocumentsResponse.GetMostIncorrectDocumentsCategoryDto.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .build();

            GetMostIncorrectDocumentsResponse.GetMostIncorrectDocumentsDto documentDto = GetMostIncorrectDocumentsResponse.GetMostIncorrectDocumentsDto.builder()
                    .documentId(document.getId())
                    .documentName(document.getName())
                    .category(categoryDto)
                    .build();

            documentsDtos.add(documentDto);
        }
        return new GetMostIncorrectDocumentsResponse(documentsDtos);
    }


    //보유한 모든 문서의 개수
    public int findPossessDocumentCount(Long memberId) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        return documents.size();
    }

    // 생성한 모든 문서
    public int findUploadedDocumentCount(Long memberId) {
        List<DocumentUpload> uploadedDocuments = documentUploadRepository.findAllByMemberId(memberId);
        return uploadedDocuments.size();
    }

    //현재 구독 사이클에 업로드한 문서 개수
    public int findUploadedDocumentCountForCurrentSubscription(Long memberId, Subscription subscription) {
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

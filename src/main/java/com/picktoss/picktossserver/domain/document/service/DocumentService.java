package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.core.sqs.SqsProvider;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.controller.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetMostIncorrectDocumentsResponse;
import com.picktoss.picktossserver.domain.document.controller.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.controller.response.SearchDocumentResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.keypoint.entity.KeyPoint;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;
import static com.picktoss.picktossserver.global.enums.DocumentStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final S3Provider s3Provider;
    private final SqsProvider sqsProvider;
    private final DocumentRepository documentRepository;

    @Value("${picktoss.default_document_s3_key}")
    private String defaultDocumentS3Key;

    @Transactional
    public Long createDocument(String documentName, MultipartFile file, Category category, Long memberId) {
        String s3Key = s3Provider.uploadFile(file);

        Integer lastOrder = documentRepository.findLastOrderByCategoryIdAndMemberId(category.getId(), memberId);
        if (lastOrder == null) {
            lastOrder = 0;
        }

        int order = lastOrder;

        Document document = Document.createDocument(
                documentName, s3Key, order + 1, UNPROCESSED, true, category
        );

        documentRepository.save(document);
        return document.getId();
    }

    @Transactional
    public void createAiPick(Long documentId, Long memberId, Subscription subscription) {
        Optional<Document> optionalDocument = documentRepository.findByDocumentIdAndMemberId(documentId, memberId);

        if (optionalDocument.isEmpty()) {
            throw new CustomException(DOCUMENT_NOT_FOUND);
        }

        Document document = optionalDocument.get();
        document.updateDocumentStatus(PROCESSING);

        sqsProvider.sendMessage(document.getS3Key(), document.getId(), subscription.getSubscriptionPlanType());
    }

    @Transactional
    public Document createDefaultDocument(Long memberId, Category category) {
        Integer lastOrder = documentRepository.findLastOrderByCategoryIdAndMemberId(category.getId(), memberId);
        if (lastOrder == null) {
            lastOrder = 0;
        }

        int order = lastOrder;

        Document document = Document.createDocument("예시 문서", defaultDocumentS3Key, order + 1, DEFAULT_DOCUMENT, false, category);

        documentRepository.save(document);

        return document;
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
                    .bookmark(keyPoint.isBookmark())
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
                .isTodayQuizIncluded(document.isTodayQuizIncluded())
                .category(categoryDto)
                .keyPoints(keyPointDtos)
                .content(content)
                .createdAt(document.getCreatedAt())
                .build();
    }

    public List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> findAllDocuments(Long memberId, Long categoryId, String documentSortOption) {
        List<Document> documents = switch (documentSortOption) {
            case "updatedAt" -> documentRepository.findAllByCategoryIdAndMemberIdOrderByUpdatedAtAsc(categoryId, memberId);
            case "name" -> documentRepository.findAllByCategoryIdAndMemberIdOrderByNameAsc(categoryId, memberId);
            case "createdAt" -> documentRepository.findAllByCategoryIdAndMemberId(categoryId, memberId);
            default -> throw new CustomException(DOCUMENT_SORT_OPTION_ERROR);
        };

        List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            DocumentStatus status = UNPROCESSED;
            if (document.getStatus() == PARTIAL_SUCCESS ||
                    document.getStatus() == PROCESSED ||
                    document.getStatus() == COMPLETELY_FAILED) {
                status = PROCESSED;
            } else if (document.getStatus() == PROCESSING) {
                status = PROCESSING;
            } else if (document.getStatus() == DEFAULT_DOCUMENT) {
                status = DEFAULT_DOCUMENT;
            }

            GetAllDocumentsResponse.GetAllDocumentsDocumentDto documentDto = GetAllDocumentsResponse.GetAllDocumentsDocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .status(status)
                    .isTodayQuizIncluded(document.isTodayQuizIncluded())
                    .createdAt(document.getCreatedAt())
                    .build();

            documentDtos.add(documentDto);
        }
        return documentDtos;
    }

    @Transactional
    public void deleteDocument(Long memberId, Long documentId) {
        Optional<Document> optionalDocument = documentRepository.findByDocumentIdAndMemberId(documentId, memberId);
        if (optionalDocument.isEmpty()) {
            return ;
        }

        Document document = optionalDocument.get();
        if (!Objects.equals(document.getCategory().getMember().getId(), memberId)) {
            throw new CustomException(UNAUTHORIZED_OPERATION_EXCEPTION);
        }

        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        for (Document d : documents) {
            if (d.getOrder() > document.getOrder()) {
                d.minusDocumentOrder();
            }
        }

        documentRepository.delete(document);
    }

    @Transactional
    public void changeDocumentOrder(Long documentId, int preDragDocumentOrder, int afterDragDocumentOrder, Long memberId) {
        if (preDragDocumentOrder > afterDragDocumentOrder) {
            List<Document> documents = documentRepository.findByOrderGreaterThanEqualAndOrderLessThanOrderByOrderAsc(
                    afterDragDocumentOrder, preDragDocumentOrder, memberId);
            for (Document document : documents) {
                document.addDocumentOrder();
            }
        } else {
            List<Document> documents = documentRepository.findByOrderGreaterThanAndOrderLessThanEqualOrderByOrderAsc(
                    preDragDocumentOrder, afterDragDocumentOrder, memberId);
            for (Document document : documents) {
                document.minusDocumentOrder();
            }
        }
        Optional<Document> optionalDocument = documentRepository.findByDocumentIdAndMemberId(documentId, memberId);

        if (optionalDocument.isEmpty()) {
            throw new CustomException(DOCUMENT_NOT_FOUND);
        }

        Document document = optionalDocument.get();
        document.updateDocumentOrder(afterDragDocumentOrder);
    }

    @Transactional
    public void moveDocumentToCategory(Long documentId, Long memberId, Category category) {
        Optional<Document> optionalDocument = documentRepository.findByDocumentIdAndMemberId(documentId, memberId);

        if (optionalDocument.isEmpty()) {
            throw new CustomException(DOCUMENT_NOT_FOUND);
        }

        Document document = optionalDocument.get();
        document.moveDocumentToCategory(category);

        Integer lastOrder = documentRepository.findLastOrderByCategoryIdAndMemberId(category.getId(), memberId);
        if (lastOrder == null) {
            lastOrder = 0;
        }

        document.updateDocumentOrder(lastOrder + 1);
    }

    public List<SearchDocumentResponse.SearchDocumentDto> searchDocument(String word, Long memberId) {

        List<SearchDocumentResponse.SearchDocumentDto> documentDtos = new ArrayList<>();

        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        for (Document document : documents) {
            String content = s3Provider.findFile(document.getS3Key());
            String documentName = document.getName();
            if (content.toLowerCase().contains(word.toLowerCase())
                    || documentName.toLowerCase().contains(word.toLowerCase())) {
                Category category = document.getCategory();
                SearchDocumentResponse.SearchDocumentCategoryDto categoryDto = SearchDocumentResponse.SearchDocumentCategoryDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build();

                SearchDocumentResponse.SearchDocumentDto documentDto = SearchDocumentResponse.SearchDocumentDto.builder()
                        .documentId(document.getId())
                        .documentName(document.getName())
                        .content(content)
                        .category(categoryDto)
                        .build();

                documentDtos.add(documentDto);
            }

        }
        return documentDtos;
    }

    public GetMostIncorrectDocumentsResponse findMostIncorrectDocuments(Long memberId) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);

        HashMap<Document, Integer> documentIncorrectAnswerCounts = new HashMap<>();

        for (Document document : documents) {
            List<Quiz> quizzes = document.getQuizzes();
            int totalIncorrectAnswerCount = 0;

            for (Quiz quiz : quizzes) {
                totalIncorrectAnswerCount += quiz.getIncorrectAnswerCount();
            }

            documentIncorrectAnswerCounts.put(document, totalIncorrectAnswerCount);
        }

        List<GetMostIncorrectDocumentsResponse.GetMostIncorrectDocumentsDto> documentsDtos = new ArrayList<>();

        HashMap<Document, Integer> top5Documents = documentIncorrectAnswerCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        for (Document document : top5Documents.keySet()) {
            Category category = document.getCategory();

            GetMostIncorrectDocumentsResponse.GetMostIncorrectDocumentsCategoryDto categoryDto = GetMostIncorrectDocumentsResponse.GetMostIncorrectDocumentsCategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();

            GetMostIncorrectDocumentsResponse.GetMostIncorrectDocumentsDto documentDto = GetMostIncorrectDocumentsResponse.GetMostIncorrectDocumentsDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .incorrectAnswerCount(top5Documents.get(document))
                    .category(categoryDto)
                    .build();

            documentsDtos.add(documentDto);
        }
        return new GetMostIncorrectDocumentsResponse(documentsDtos);
    }

    @Transactional
    public void updateDocumentContent(Long documentId, Long memberId, String name, MultipartFile file) {
        Optional<Document> optionalDocument = documentRepository.findByDocumentIdAndMemberId(documentId, memberId);

        if (optionalDocument.isEmpty()) {
            throw new CustomException(DOCUMENT_NOT_FOUND);
        }

        Document document = optionalDocument.get();
        s3Provider.deleteFile(document.getS3Key());

        String s3Key = s3Provider.uploadFile(file);
        document.updateDocumentS3Key(s3Key);
        document.updateDocumentName(name);
        document.updateDocumentStatus(KEYPOINT_UPDATE_POSSIBLE);
    }

    @Transactional
    public void updateDocumentName(Long documentId, Long memberId, String documentName) {
        Optional<Document> optionalDocument = documentRepository.findByDocumentIdAndMemberId(documentId, memberId);

        if (optionalDocument.isEmpty()) {
            throw new CustomException(DOCUMENT_NOT_FOUND);
        }

        Document document = optionalDocument.get();
        document.updateDocumentName(documentName);
    }

    @Transactional
    public void reUploadDocument(Long documentId, Long memberId, Subscription subscription) {
        Optional<Document> optionalDocument = documentRepository.findByDocumentIdAndMemberId(documentId, memberId);

        if (optionalDocument.isEmpty()) {
            throw new CustomException(DOCUMENT_NOT_FOUND);
        }

        Document document = optionalDocument.get();
        sqsProvider.sendMessage(document.getS3Key(), document.getId(), subscription.getSubscriptionPlanType());
    }

    //보유한 모든 문서의 개수
    public int findPossessDocumentCount(Long memberId) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        int possessDocumentCount = documents.size();
        for (Document document : documents) {
            if (document.getStatus() == DEFAULT_DOCUMENT) {
                return possessDocumentCount - 1;
            }
        }
        return possessDocumentCount;
    }


    //현재 구독 사이클에 업로드한 문서 개수
    public int findUploadedDocumentCountForCurrentSubscription(Long memberId, Subscription subscription) {
        List<Document> uploadedDocuments = documentRepository.findAllByMemberId(memberId);

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

    public List<Document> findAllByCategoryIdAndMemberId(Long categoryId, Long memberId) {
        return documentRepository.findAllByCategoryIdAndMemberId(categoryId, memberId);
    }

    public List<Document> findAllByMemberId(Long memberId) {
        return documentRepository.findAllByMemberId(memberId);
    }

    public Document findByDocumentIdAndMemberId(Long documentId, Long memberId) {
        return documentRepository.findByDocumentIdAndMemberId(documentId, memberId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));
    }
}

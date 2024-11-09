package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.core.sqs.SqsProvider;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.document.controller.response.*;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.global.enums.document.DocumentSortOption;
import com.picktoss.picktossserver.global.enums.document.DocumentStatus;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;
import static com.picktoss.picktossserver.global.enums.document.DocumentStatus.DEFAULT_DOCUMENT;
import static com.picktoss.picktossserver.global.enums.document.DocumentStatus.UNPROCESSED;

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
    public Document createDocument(String documentName, Directory directory, String s3Key) {
        Document document = Document.createDocument(
                documentName, s3Key, UNPROCESSED, true, directory
        );

        documentRepository.save(document);
        return document;
    }

    @Transactional
    public Document createDefaultDocument(Directory directory) {
        Document document = Document.createDefaultDocument(defaultDocumentS3Key, directory);
        documentRepository.save(document);
        return document;
    }

    public GetSingleDocumentResponse findSingleDocument(Long memberId, Long documentId) {
        Document document = documentRepository.findDocumentWithQuizzesByDocumentIdAndMemberId(documentId, memberId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        String content = s3Provider.findFile(document.getS3Key());
        int characterCount = content.length();

        Set<Quiz> quizzes = document.getQuizzes();
        List<GetSingleDocumentResponse.GetSingleDocumentQuizDto> quizDtos = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                if (options.isEmpty()) {
                    continue;
                }
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            GetSingleDocumentResponse.GetSingleDocumentQuizDto quizDto = GetSingleDocumentResponse.GetSingleDocumentQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .build();

            quizDtos.add(quizDto);
        }

        GetSingleDocumentResponse.GetSingleDocumentDirectoryDto directoryDto = GetSingleDocumentResponse.GetSingleDocumentDirectoryDto.builder()
                .id(document.getDirectory().getId())
                .name(document.getDirectory().getName())
                .build();

        DocumentStatus documentStatus = document.updateDocumentStatusClientResponse(document.getStatus());

        return GetSingleDocumentResponse.builder()
                .id(document.getId())
                .documentName(document.getName())
                .status(documentStatus)
                .directory(directoryDto)
                .content(content)
                .characterCount(characterCount)
                .totalQuizCount(quizDtos.size())
                .updatedAt(document.getUpdatedAt())
                .quizzes(quizDtos)
                .build();
    }

    public List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> findAllDocumentsInDirectory(
            Long memberId, Long directoryId, DocumentSortOption documentSortOption, List<QuizSetQuiz> quizSetQuizzes
    ) {
        List<Document> documents;

        if (directoryId == null) {
            documents = (documentSortOption == DocumentSortOption.CREATED_AT)
                    ? documentRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId)
                    : documentRepository.findAllByMemberIdOrderByUpdatedAtDesc(memberId);
        } else {
            documents = (documentSortOption == DocumentSortOption.CREATED_AT)
                    ? documentRepository.findAllByDirectoryIdAndMemberIdOrderByCreatedAtDesc(directoryId, memberId)
                    : documentRepository.findAllByDirectoryIdAndMemberIdOrderByUpdatedAtDesc(directoryId, memberId);
        }

        Map<Long, Integer> reviewNeededDocumentIdAndQuizCount = new HashMap<>();
        Set<Long> processedQuizIds = new HashSet<>(); // 이미 처리된 Quiz ID를 추적하는 Set

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            QuizSet quizSet = quizSetQuiz.getQuizSet();
            if (!quizSet.isSolved()) {
                continue;
            }

            Long quizId = quizSetQuiz.getQuiz().getId();
            if (processedQuizIds.contains(quizId)) {
                continue; // 이미 처리된 퀴즈인 경우 if 문을 생략하고 다음 퀴즈로 넘어감
            }

            // 퀴즈가 처리된 것으로 표시
            processedQuizIds.add(quizId);

            // 정답이 아니거나 문제를 푸는데 20초이상 걸렸다면
            if (!quizSetQuiz.getIsAnswer() || quizSetQuiz.getElapsedTimeMs() >= 20000) {
                Long documentId = quizSetQuiz.getQuiz().getDocument().getId();
                reviewNeededDocumentIdAndQuizCount.put(documentId, reviewNeededDocumentIdAndQuizCount.getOrDefault(documentId, 0) + 1);
            }
        }

        List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            Integer reviewNeededQuizCount = reviewNeededDocumentIdAndQuizCount.get(document.getId());
            DocumentStatus documentStatus = document.updateDocumentStatusClientResponse(document.getStatus());
            String content = s3Provider.findFile(document.getS3Key());
            int characterCount = content.length();

            Directory directory = document.getDirectory();
            GetAllDocumentsResponse.GetAllDocumentsDirectoryDto directoryDto = GetAllDocumentsResponse.GetAllDocumentsDirectoryDto.builder()
                    .name(directory.getName())
                    .tag(directory.getTag())
                    .build();

            GetAllDocumentsResponse.GetAllDocumentsDocumentDto documentDto = GetAllDocumentsResponse.GetAllDocumentsDocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .characterCount(characterCount)
                    .status(documentStatus)
                    .totalQuizCount(document.getQuizzes().size())
                    .createdAt(document.getCreatedAt())
                    .updatedAt(document.getUpdatedAt())
                    .reviewNeededQuizCount(reviewNeededQuizCount)
                    .directory(directoryDto)
                    .build();

            documentDtos.add(documentDto);
        }
        return documentDtos;
    }

    @Transactional
    public List<Document> deleteDocument(Long memberId, List<Long> documentIds) {
        List<Document> deleteDocuments = documentRepository.findByDocumentIdsInAndMemberId(documentIds, memberId);

        if (deleteDocuments.isEmpty()) {
            throw new CustomException(DOCUMENT_NOT_FOUND);
        }

        documentRepository.deleteAll(deleteDocuments);
        return deleteDocuments;
    }

    @Transactional
    public void moveDocumentToDirectory(List<Long> documentIds, Long memberId, Directory directory) {
        List<Document> documents = documentRepository.findByDocumentIdsInAndMemberId(documentIds, memberId);
        for (Document document : documents) {
            document.moveDocumentToDirectory(directory);
        }
    }

    public SearchDocumentResponse searchDocumentByKeyword(String keyword, Long memberId) {

        List<SearchDocumentResponse.SearchDocumentDto> documentDtos = new ArrayList<>();
        List<SearchDocumentResponse.SearchDocumentQuizDto> quizDtos = new ArrayList<>();

        List<Document> documents = documentRepository.findAllByMemberId(memberId);

        for (Document document : documents) {
            Directory directory = document.getDirectory();
            String content = s3Provider.findFile(document.getS3Key());
            String documentName = document.getName();
            if (content.toLowerCase().contains(keyword.toLowerCase())
                    || documentName.toLowerCase().contains(keyword.toLowerCase())
            ) {
                SearchDocumentResponse.SearchDocumentDirectoryDto directoryDto = SearchDocumentResponse.SearchDocumentDirectoryDto.builder()
                        .id(directory.getId())
                        .name(directory.getName())
                        .build();

                SearchDocumentResponse.SearchDocumentDto documentDto = SearchDocumentResponse.SearchDocumentDto.builder()
                        .documentId(document.getId())
                        .documentName(document.getName())
                        .content(content)
                        .directory(directoryDto)
                        .build();

                documentDtos.add(documentDto);
            }

            Set<Quiz> quizzes = document.getQuizzes();
            for (Quiz quiz : quizzes) {
                if (quiz.getQuestion().toLowerCase().contains(keyword.toLowerCase())
                        || quiz.getAnswer().toLowerCase().contains(keyword.toLowerCase())
                        || quiz.getExplanation().toLowerCase().contains(keyword.toLowerCase())
                ) {
                    SearchDocumentResponse.SearchDocumentQuizDto quizDto = SearchDocumentResponse.SearchDocumentQuizDto.builder()
                            .id(quiz.getId())
                            .question(quiz.getQuestion())
                            .answer(quiz.getAnswer())
                            .directoryName(directory.getName())
                            .documentName(document.getName())
                            .build();

                    quizDtos.add(quizDto);
                }
            }
        }
        return new SearchDocumentResponse(documentDtos, quizDtos);
    }

    public GetDocumentsNeedingReviewResponse findDocumentsNeedingReview(Long memberId, List<QuizSetQuiz> quizSetQuizzes) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        HashMap<Document, Integer> documentsNeedingReviewCountMap = new LinkedHashMap<>();
        for (Document document : documents) {
            documentsNeedingReviewCountMap.put(document, documentsNeedingReviewCountMap.getOrDefault(document, 0));
        }

        Set<Long> processedQuizIds = new HashSet<>(); // 이미 처리된 Quiz ID를 추적하는 Set
        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            QuizSet quizSet = quizSetQuiz.getQuizSet();
            if (!quizSet.isSolved()) {
                continue;
            }

            Long quizId = quizSetQuiz.getQuiz().getId();
            if (processedQuizIds.contains(quizId)) {
                continue; // 이미 처리된 퀴즈인 경우 if 문을 생략하고 다음 퀴즈로 넘어감
            }

            // 퀴즈가 처리된 것으로 표시
            processedQuizIds.add(quizId);

            if (!quizSetQuiz.getIsAnswer() || quizSetQuiz.getElapsedTimeMs() >= 20000) {
                Document document = quizSetQuiz.getQuiz().getDocument();
                documentsNeedingReviewCountMap.put(document, documentsNeedingReviewCountMap.get(document) + 1);
            }
        }
        List<GetDocumentsNeedingReviewResponse.GetReviewNeededDocumentsDto> documentsDtos = new ArrayList<>();

        HashMap<Document, Integer> top5Documents = documentsNeedingReviewCountMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        for (Document document : top5Documents.keySet()) {
            Directory directory = document.getDirectory();

            GetDocumentsNeedingReviewResponse.GetReviewNeededDocumentsDirectoryDto directoryDto = GetDocumentsNeedingReviewResponse.GetReviewNeededDocumentsDirectoryDto.builder()
                    .id(directory.getId())
                    .name(directory.getName())
                    .build();

            GetDocumentsNeedingReviewResponse.GetReviewNeededDocumentsDto documentDto = GetDocumentsNeedingReviewResponse.GetReviewNeededDocumentsDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .reviewNeededQuizCount(top5Documents.get(document))
                    .directory(directoryDto)
                    .build();

            documentsDtos.add(documentDto);
        }
        return new GetDocumentsNeedingReviewResponse(documentsDtos);
    }

    @Transactional
    public void updateDocumentContent(Long documentId, Long memberId, String name, String s3Key) {
        Document document = documentRepository.findByDocumentIdAndMemberId(documentId, memberId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        document.updateDocumentS3KeyByUpdatedContent(s3Key);
        document.updateDocumentName(name);
    }

    @Transactional
    public void updateDocumentName(Long documentId, Long memberId, String documentName) {
        Document document = documentRepository.findByDocumentIdAndMemberId(documentId, memberId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));

        document.updateDocumentName(documentName);
    }

    @Transactional
    public void selectDocumentToNotGenerateByTodayQuiz(Map<Long, Boolean> documentIdTodayQuizMap, Long memberId) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        for (Document document : documents) {
            if (documentIdTodayQuizMap.containsKey(document.getId())) {
                document.updateDocumentIsTodayQuizIncluded(documentIdTodayQuizMap.get(document.getId()));
            }
        }
    }

    public IntegratedSearchResponse integratedSearchByKeyword(Long memberId, String keyword, List<Collection> collections) {
        List<Document> documents = documentRepository.findAllWithDirectoryAndQuizzes(memberId);
        List<IntegratedSearchResponse.IntegratedSearchDocumentDto> documentDtos = new ArrayList<>();
        List<IntegratedSearchResponse.IntegratedSearchQuizDto> quizDtos = new ArrayList<>();
        List<IntegratedSearchResponse.IntegratedSearchCollectionDto> collectionDtos = new ArrayList<>();

        for (Document document : documents) {
            Directory directory = document.getDirectory();
            String content = s3Provider.findFile(document.getS3Key());
            String documentName = document.getName();
            if (content.toLowerCase().contains(keyword.toLowerCase())
                    || documentName.toLowerCase().contains(keyword.toLowerCase())
            ) {
                IntegratedSearchResponse.IntegratedSearchDirectoryDto directoryDto = IntegratedSearchResponse.IntegratedSearchDirectoryDto.builder()
                        .id(directory.getId())
                        .name(directory.getName())
                        .build();

                IntegratedSearchResponse.IntegratedSearchDocumentDto documentDto = IntegratedSearchResponse.IntegratedSearchDocumentDto.builder()
                        .documentId(document.getId())
                        .documentName(document.getName())
                        .content(content)
                        .directory(directoryDto)
                        .build();

                documentDtos.add(documentDto);
            }

            Set<Quiz> quizzes = document.getQuizzes();
            for (Quiz quiz : quizzes) {
                if (quiz.getQuestion().toLowerCase().contains(keyword.toLowerCase())
                        || quiz.getAnswer().toLowerCase().contains(keyword.toLowerCase())
                        || quiz.getExplanation().toLowerCase().contains(keyword.toLowerCase())
                ) {
                    IntegratedSearchResponse.IntegratedSearchQuizDto quizDto = IntegratedSearchResponse.IntegratedSearchQuizDto.builder()
                            .id(quiz.getId())
                            .question(quiz.getQuestion())
                            .answer(quiz.getAnswer())
                            .directoryName(directory.getName())
                            .documentName(document.getName())
                            .build();

                    quizDtos.add(quizDto);
                }
            }
        }

        for (Collection collection : collections) {
            IntegratedSearchResponse.IntegratedSearchCollectionDto collectionDto = IntegratedSearchResponse.IntegratedSearchCollectionDto.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .emoji(collection.getEmoji())
                    .bookmarkCount(collection.getCollectionBookmarks().size())
                    .collectionField(collection.getCollectionField())
                    .memberName(collection.getMember().getName())
                    .quizCount(collection.getCollectionQuizzes().size())
                    .build();

            collectionDtos.add(collectionDto);

        }

        return new IntegratedSearchResponse(documentDtos, quizDtos, collectionDtos);
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

    public List<Document> findAllByMemberId(Long memberId) {
        return documentRepository.findAllByMemberId(memberId);
    }
}

package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.document.dto.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.GetBookmarkedDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.GetIsNotPublicDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.GetSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentBookmarkRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.document.BookmarkedDocumentSortOption;
import com.picktoss.picktossserver.global.enums.document.DocumentSortOption;
import com.picktoss.picktossserver.global.enums.document.QuizGenerationStatus;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentReadService {

    private final S3Provider s3Provider;
    private final DocumentRepository documentRepository;
    private final DocumentBookmarkRepository documentBookmarkRepository;

    //단일 문서 가져오기
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

        int bookmarkCount = 0;

        if (document.getIsPublic()) {
            Set<DocumentBookmark> documentBookmarks = document.getDocumentBookmarks();
            if (documentBookmarks != null && !documentBookmarks.isEmpty()) {
                bookmarkCount = documentBookmarks.size();
            }
        }

        QuizGenerationStatus quizGenerationStatus = document.updateDocumentStatusClientResponse(document.getQuizGenerationStatus());

        return GetSingleDocumentResponse.builder()
                .id(document.getId())
                .name(document.getName())
                .emoji(document.getEmoji())
                .content(content)
                .bookmarkCount(bookmarkCount)
                .isPublic(document.getIsPublic())
                .characterCount(characterCount)
                .totalQuizCount(quizDtos.size())
                .createdAt(document.getCreatedAt())
                .documentType(document.getDocumentType())
                .category(document.getCategory().getName())
                .quizGenerationStatus(quizGenerationStatus)
                .quizzes(quizDtos)
                .build();
    }

    //모든 문서 가져오기
    public GetAllDocumentsResponse findAllDocuments(Long memberId, DocumentSortOption documentSortOption) {
        List<Document> documents;

        if (documentSortOption == DocumentSortOption.CREATED_AT) {
            documents = documentRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
        } else if (documentSortOption == DocumentSortOption.NAME) {
            documents = documentRepository.findAllByMemberIdOrderByNameAsc(memberId);
        } else if (documentSortOption == DocumentSortOption.QUIZ_COUNT) {
            documents = documentRepository.findAllByMemberIdOrderByQuizCountDesc(memberId);
        } else {
            documents = documentRepository.findAllOrderByWrongAnswerCount(memberId);
        }

        List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            int reviewNeededQuizCount = 0;
            Set<Quiz> quizzes = document.getQuizzes();
            for (Quiz quiz : quizzes) {
                boolean reviewNeeded = quiz.isReviewNeeded();
                if (reviewNeeded) {
                    reviewNeededQuizCount += 1;
                }
            }
            String content = s3Provider.findFile(document.getS3Key());
            String previewContent = content.substring(0, 100);

            int bookmarkCount = 0;

            if (document.getIsPublic()) {
                Set<DocumentBookmark> documentBookmarks = document.getDocumentBookmarks();
                if (documentBookmarks != null && !documentBookmarks.isEmpty()) {
                    bookmarkCount = documentBookmarks.size();
                }
            }

            GetAllDocumentsResponse.GetAllDocumentsDocumentDto documentDto = GetAllDocumentsResponse.GetAllDocumentsDocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .isPublic(document.getIsPublic())
                    .emoji(document.getEmoji())
                    .tryCount(document.getTryCount())
                    .bookmarkCount(bookmarkCount)
                    .previewContent(previewContent)
                    .totalQuizCount(quizzes.size())
                    .reviewNeededQuizCount(reviewNeededQuizCount)
                    .build();

            documentDtos.add(documentDto);
        }

        return new GetAllDocumentsResponse(documentDtos);
    }

    //북마크된 문서 가져오기
    public GetBookmarkedDocumentsResponse findBookmarkedDocuments(Long memberId, BookmarkedDocumentSortOption documentSortOption) {
        List<DocumentBookmark> documentBookmarks;

        if (documentSortOption == BookmarkedDocumentSortOption.QUIZ_COUNT) {
            documentBookmarks = documentBookmarkRepository.findAllByMemberIdAndIsPublicTrueOrderByQuizCountDesc(memberId);
        } else {
            documentBookmarks = documentBookmarkRepository.findAllByMemberIdAndIsPublicTrueOrderByCreatedAtDesc(memberId);
        }

        List<GetBookmarkedDocumentsResponse.GetBookmarkedDocumentsDto> documentsDtos = new ArrayList<>();
        for (DocumentBookmark documentBookmark : documentBookmarks) {
            Document document = documentBookmark.getDocument();

            Set<Quiz> quizzes = document.getQuizzes();
            List<Quiz> quizList = new ArrayList<>(quizzes);
            Quiz quiz = quizList.getFirst();
            String question = quiz.getQuestion();

            GetBookmarkedDocumentsResponse.GetBookmarkedDocumentsDto documentsDto = GetBookmarkedDocumentsResponse.GetBookmarkedDocumentsDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .emoji(document.getEmoji())
                    .previewContent(question)
                    .tryCount(document.getTryCount())
                    .bookmarkCount(document.getDocumentBookmarks().size())
                    .totalQuizCount(document.getQuizzes().size())
                    .build();

            documentsDtos.add(documentsDto);
        }

        return new GetBookmarkedDocumentsResponse(documentsDtos);
    }

    public GetIsNotPublicDocumentsResponse findIsNotPublicDocuments(Long memberId) {
        List<Document> documents = documentRepository.findAllByIsNotPublicAndMemberId(memberId);
        return new GetIsNotPublicDocumentsResponse(documents.size());
    }
}

package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.document.dto.response.SearchDocumentsResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentBookmarkRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentSearchService {

    private final S3Provider s3Provider;
    private final DocumentRepository documentRepository;
    private final DocumentBookmarkRepository documentBookmarkRepository;

    //문서 검색
    public SearchDocumentsResponse searchDocumentsByKeyword(String keyword, Long memberId) {
        List<SearchDocumentsResponse.SearchDocumentsDto> documentDtos = new ArrayList<>();
        List<SearchDocumentsResponse.SearchBookmarkDocumentsDto> bookmarkedDocumentDtos = new ArrayList<>();

        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        List<DocumentBookmark> documentBookmarks = documentBookmarkRepository.findAllByMemberId(memberId);

        for (Document document : documents) {
            List<SearchDocumentsResponse.SearchDocumentsQuizDto> quizDtos = new ArrayList<>();

            String content = s3Provider.findFile(document.getS3Key());
            String documentName = document.getName();

            int bookmarkCount = 0;
            if (document.getIsPublic()) {
                Set<DocumentBookmark> documentBookmarkList = document.getDocumentBookmarks();
                if (documentBookmarkList != null && !documentBookmarkList.isEmpty()) {
                    bookmarkCount = documentBookmarkList.size();
                }
            }
            Set<Quiz> quizzes = document.getQuizzes();
            for (Quiz quiz : quizzes) {
                if (quiz.getQuestion().toLowerCase().contains(keyword.toLowerCase())
                        || quiz.getAnswer().toLowerCase().contains(keyword.toLowerCase())
                        || quiz.getExplanation().toLowerCase().contains(keyword.toLowerCase())
                        || content.toLowerCase().contains(keyword.toLowerCase())
                        || documentName.toLowerCase().contains(keyword.toLowerCase())
                ) {
                    SearchDocumentsResponse.SearchDocumentsQuizDto quizDto = SearchDocumentsResponse.SearchDocumentsQuizDto.builder()
                            .question(quiz.getQuestion())
                            .answer(quiz.getAnswer())
                            .explanation(quiz.getExplanation())
                            .build();

                    quizDtos.add(quizDto);

                    SearchDocumentsResponse.SearchDocumentsDto documentDto = SearchDocumentsResponse.SearchDocumentsDto.builder()
                            .id(document.getId())
                            .name(document.getName())
                            .emoji(document.getEmoji())
                            .content(content)
                            .isPublic(document.getIsPublic())
                            .tryCount(document.getTryCount())
                            .bookmarkCount(bookmarkCount)
                            .totalQuizCount(document.getQuizzes().size())
                            .quizzes(quizDtos)
                            .build();

                    documentDtos.add(documentDto);
                    break;
                }
            }
        }

        for (DocumentBookmark documentBookmark : documentBookmarks) {
            Document document = documentBookmark.getDocument();
            List<SearchDocumentsResponse.SearchDocumentsQuizDto> quizDtos = new ArrayList<>();

            String documentName = document.getName();

            int bookmarkCount = 0;
            if (document.getIsPublic()) {
                Set<DocumentBookmark> documentBookmarkList = document.getDocumentBookmarks();
                if (documentBookmarkList != null && !documentBookmarkList.isEmpty()) {
                    bookmarkCount = documentBookmarkList.size();
                }
            }
            Set<Quiz> quizzes = document.getQuizzes();
            for (Quiz quiz : quizzes) {
                if (quiz.getQuestion().toLowerCase().contains(keyword.toLowerCase())
                        || quiz.getAnswer().toLowerCase().contains(keyword.toLowerCase())
                        || quiz.getExplanation().toLowerCase().contains(keyword.toLowerCase())
                        || documentName.toLowerCase().contains(keyword.toLowerCase())
                ) {
                    SearchDocumentsResponse.SearchDocumentsQuizDto quizDto = SearchDocumentsResponse.SearchDocumentsQuizDto.builder()
                            .question(quiz.getQuestion())
                            .answer(quiz.getAnswer())
                            .explanation(quiz.getExplanation())
                            .build();

                    quizDtos.add(quizDto);

                    SearchDocumentsResponse.SearchBookmarkDocumentsDto bookmarkedDocumentsDto = SearchDocumentsResponse.SearchBookmarkDocumentsDto.builder()
                            .id(document.getId())
                            .name(document.getName())
                            .emoji(document.getEmoji())
                            .tryCount(document.getTryCount())
                            .bookmarkCount(bookmarkCount)
                            .totalQuizCount(document.getQuizzes().size())
                            .quizzes(quizDtos)
                            .build();

                    bookmarkedDocumentDtos.add(bookmarkedDocumentsDto);
                    break;
                }
            }
        }
        return new SearchDocumentsResponse(documentDtos, bookmarkedDocumentDtos);
    }
}

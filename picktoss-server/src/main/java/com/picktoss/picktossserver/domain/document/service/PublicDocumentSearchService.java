package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.document.dto.response.GetPublicDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.SearchDocumentsResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicDocumentSearchService {

    private final S3Provider s3Provider;
    private final DocumentRepository documentRepository;
    private final QuizRepository quizRepository;

    public GetPublicDocumentsResponse findPublicDocuments(Long categoryId, Long memberId, int page, int pageSize) {
        if (pageSize < 1) {
            throw new CustomException(ErrorInfo.DOCUMENT_PAGE_SET_ERROR);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Document> documents;
        if (categoryId == null) {
            documents = documentRepository.findAllByIsPublic(pageable);
        } else {
            documents = documentRepository.findAllByIsPublicAndCategoryId(categoryId, pageable);
        }

        int totalPages = documents.getTotalPages();
        long totalDocuments = documents.getTotalElements();

        List<GetPublicDocumentsResponse.GetPublicDocumentsDto> documentsDtos = new ArrayList<>();
        for (Document document : documents) {
            Set<Quiz> quizzes = document.getQuizzes();

            List<GetPublicDocumentsResponse.GetPublicDocumentQuizDto> quizDtos = new ArrayList<>();
            for (Quiz quiz : quizzes) {
                GetPublicDocumentsResponse.GetPublicDocumentQuizDto quizDto = GetPublicDocumentsResponse.GetPublicDocumentQuizDto.builder()
                        .id(quiz.getId())
                        .question(quiz.getQuestion())
                        .build();

                quizDtos.add(quizDto);
            }

            int bookmarkCount = 0;
            boolean isBookmarked = false;

            Set<DocumentBookmark> documentBookmarks = document.getDocumentBookmarks();
            if (documentBookmarks != null && !documentBookmarks.isEmpty()) {
                bookmarkCount = documentBookmarks.size();

                for (DocumentBookmark documentBookmark : documentBookmarks) {
                    if (Objects.equals(documentBookmark.getMember().getId(), memberId)) {
                        isBookmarked = true;
                    }
                }
            }

            boolean isOwner = false;
            Member member = document.getDirectory().getMember();
            if (Objects.equals(memberId, member.getId())) {
                isOwner = true;
            }

            GetPublicDocumentsResponse.GetPublicDocumentsDto documentsDto = GetPublicDocumentsResponse.GetPublicDocumentsDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .emoji(document.getEmoji())
                    .creator(member.getName())
                    .category(document.getCategory().getName())
                    .tryCount(document.getTryCount())
                    .isBookmarked(isBookmarked)
                    .isOwner(isOwner)
                    .bookmarkCount(bookmarkCount)
                    .totalQuizCount(document.getQuizzes().size())
                    .quizzes(quizDtos)
                    .build();

            documentsDtos.add(documentsDto);
        }
        return new GetPublicDocumentsResponse(totalPages, totalDocuments, documentsDtos);
    }

    public SearchDocumentsResponse searchPublicDocuments(String keyword, Long memberId) {
        List<Document> documents = documentRepository.findAllByIsPublicOrOwnerAndKeyword(keyword, memberId);

        List<SearchDocumentsResponse.SearchDocumentsDto> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            List<SearchDocumentsResponse.SearchDocumentsQuizDto> quizDtos = new ArrayList<>();

            String content = s3Provider.findFile(document.getS3Key());
            String documentName = document.getName();

            boolean isOwner = Objects.equals(memberId, document.getDirectory().getMember().getId());
            boolean isBookmarked = Optional.ofNullable(document.getDocumentBookmarks())
                    .orElse(Collections.emptySet())
                    .stream()
                    .anyMatch(bookmark -> Objects.equals(memberId, bookmark.getMember().getId()));

            int bookmarkCount = 0;
            if (document.getIsPublic()) {
                Set<DocumentBookmark> documentBookmarkList = document.getDocumentBookmarks();
                if (documentBookmarkList != null && !documentBookmarkList.isEmpty()) {
                    bookmarkCount = documentBookmarkList.size();
                }
            }

            for (Quiz quiz : document.getQuizzes()) {
                if (quiz.getQuestion().toLowerCase().contains(keyword.toLowerCase())
                        || quiz.getAnswer().toLowerCase().contains(keyword.toLowerCase())
                        || quiz.getExplanation().toLowerCase().contains(keyword.toLowerCase())
                        || content.toLowerCase().contains(keyword.toLowerCase())
                        || documentName.toLowerCase().contains(keyword.toLowerCase())) {

                    SearchDocumentsResponse.SearchDocumentsQuizDto quizDto =
                            SearchDocumentsResponse.SearchDocumentsQuizDto.builder()
                                    .question(quiz.getQuestion())
                                    .answer(quiz.getAnswer())
                                    .explanation(quiz.getExplanation())
                                    .build();

                    quizDtos.add(quizDto);

                    SearchDocumentsResponse.SearchDocumentsDto documentDto =
                            SearchDocumentsResponse.SearchDocumentsDto.builder()
                                    .id(document.getId())
                                    .name(document.getName())
                                    .emoji(document.getEmoji())
                                    .content(content)
                                    .isOwner(isOwner)
                                    .isPublic(document.getIsPublic())
                                    .isBookmarked(isBookmarked)
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

        List<SearchDocumentsResponse.SearchDocumentsDto> quizzesByKeyword = findQuizzesByKeyword(keyword, memberId);
        documentDtos.addAll(quizzesByKeyword);

        return new SearchDocumentsResponse(documentDtos);
    }

    private List<SearchDocumentsResponse.SearchDocumentsDto> findQuizzesByKeyword(String keyword, Long memberId) {
        List<Quiz> quizzes = quizRepository.findAllByKeyword(keyword);
        HashMap<Document, Quiz> documentQuizHashMap = new HashMap<>();

        for (Quiz quiz : quizzes) {
            Document document = quiz.getDocument();
            if (!documentQuizHashMap.containsKey(document)) {
                documentQuizHashMap.put(document, quiz);
            }
        }

        List<SearchDocumentsResponse.SearchDocumentsDto> documentDtos = new ArrayList<>();
        for (Document document : documentQuizHashMap.keySet()) {
            Quiz quiz = documentQuizHashMap.get(document);
            List<SearchDocumentsResponse.SearchDocumentsQuizDto> quizDtos = new ArrayList<>();

            String content = s3Provider.findFile(document.getS3Key());

            boolean isOwner = Objects.equals(memberId, document.getDirectory().getMember().getId());
            boolean isBookmarked = Optional.ofNullable(document.getDocumentBookmarks())
                    .orElse(Collections.emptySet())
                    .stream()
                    .anyMatch(bookmark -> Objects.equals(memberId, bookmark.getMember().getId()));

            int bookmarkCount = 0;
            if (document.getIsPublic()) {
                Set<DocumentBookmark> documentBookmarkList = document.getDocumentBookmarks();
                if (documentBookmarkList != null && !documentBookmarkList.isEmpty()) {
                    bookmarkCount = documentBookmarkList.size();
                }
            }

            SearchDocumentsResponse.SearchDocumentsQuizDto quizDto =
                    SearchDocumentsResponse.SearchDocumentsQuizDto.builder()
                            .question(quiz.getQuestion())
                            .answer(quiz.getAnswer())
                            .explanation(quiz.getExplanation())
                            .build();

            quizDtos.add(quizDto);

            SearchDocumentsResponse.SearchDocumentsDto documentDto =
                    SearchDocumentsResponse.SearchDocumentsDto.builder()
                            .id(document.getId())
                            .name(document.getName())
                            .emoji(document.getEmoji())
                            .content(content)
                            .isOwner(isOwner)
                            .isPublic(document.getIsPublic())
                            .isBookmarked(isBookmarked)
                            .tryCount(document.getTryCount())
                            .bookmarkCount(bookmarkCount)
                            .totalQuizCount(document.getQuizzes().size())
                            .quizzes(quizDtos)
                            .build();

            documentDtos.add(documentDto);
            break;
        }
        return documentDtos;
    }
}

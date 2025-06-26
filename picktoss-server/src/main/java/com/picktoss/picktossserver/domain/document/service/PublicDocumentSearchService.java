package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.document.dto.response.GetPublicDocumentsResponse;
import com.picktoss.picktossserver.domain.document.dto.response.SearchPublicDocumentsResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
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

    private final DocumentRepository documentRepository;

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

    public SearchPublicDocumentsResponse searchPublicDocuments(String keyword, Long memberId) {
        List<Document> documents = documentRepository.findAllByIsPublicAndKeyword(keyword);

        List<SearchPublicDocumentsResponse.SearchPublicDocumentsDto> publicDocumentsDtos = new ArrayList<>();
        for (Document document : documents) {
            boolean isBookmarked = false;
            int bookmarkCount = 0;
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

            SearchPublicDocumentsResponse.SearchPublicDocumentsDto publicDocumentsDto = SearchPublicDocumentsResponse.SearchPublicDocumentsDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .emoji(document.getEmoji())
                    .category(document.getCategory().getName())
                    .creatorName(document.getDirectory().getMember().getName())
                    .isOwner(isOwner)
                    .isBookmarked(isBookmarked)
                    .tryCount(document.getTryCount())
                    .bookmarkCount(bookmarkCount)
                    .totalQuizCount(document.getQuizzes().size())
                    .build();

            publicDocumentsDtos.add(publicDocumentsDto);
        }

        return new SearchPublicDocumentsResponse(publicDocumentsDtos);
    }
}

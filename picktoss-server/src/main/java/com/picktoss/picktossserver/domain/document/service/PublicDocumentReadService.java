package com.picktoss.picktossserver.domain.document.service;


import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.document.dto.response.GetPublicSingleDocumentResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicDocumentReadService {

    private final DocumentRepository documentRepository;

    public GetPublicSingleDocumentResponse findIsPublicSingleDocument(Long documentId, Long memberId) {
        Document document = documentRepository.findByDocumentIdAndIsPublic(documentId)
                .orElseThrow(() -> new CustomException(ErrorInfo.DOCUMENT_NOT_FOUND));

        if (!document.getIsPublic()) {
            throw new CustomException(ErrorInfo.CANNOT_VIEW_UNPUBLISHED_DOCUMENT);
        }

        List<GetPublicSingleDocumentResponse.GetPublicSingleDocumentQuizDto> quizDtos = new ArrayList<>();

        Set<Quiz> quizzes = document.getQuizzes();
        for (Quiz quiz : quizzes) {
            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            GetPublicSingleDocumentResponse.GetPublicSingleDocumentQuizDto quizDto = GetPublicSingleDocumentResponse.GetPublicSingleDocumentQuizDto.builder()
                    .id(quiz.getId())
                    .answer(quiz.getAnswer())
                    .question(quiz.getQuestion())
                    .explanation(quiz.getExplanation())
                    .quizType(quiz.getQuizType())
                    .options(optionList)
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

        return GetPublicSingleDocumentResponse.builder()
                .id(document.getId())
                .creator(document.getDirectory().getMember().getName())
                .name(document.getName())
                .emoji(document.getEmoji())
                .category(document.getCategory().getName())
                .tryCount(document.getTryCount())
                .bookmarkCount(bookmarkCount)
                .isOwner(isOwner)
                .isBookmarked(isBookmarked)
                .totalQuizCount(document.getQuizzes().size())
                .createdAt(document.getCreatedAt())
                .quizzes(quizDtos)
                .build();
    }
}

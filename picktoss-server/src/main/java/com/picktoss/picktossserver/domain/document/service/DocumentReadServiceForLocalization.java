package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.domain.document.dto.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentLocalizationRepository;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.document.DocumentSortOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentReadServiceForLocalization {

    private final DocumentLocalizationRepository documentLocalizationRepository;

    public GetAllDocumentsResponse findAllDocumentsForLocalization(
            Long memberId, DocumentSortOption documentSortOption, String language
    ) {
        List<Document> documents;

        if (documentSortOption == DocumentSortOption.CREATED_AT) {
            documents = documentLocalizationRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
        } else if (documentSortOption == DocumentSortOption.NAME) {
            documents = documentLocalizationRepository.findAllByMemberIdOrderByNameAsc(memberId);
        } else if (documentSortOption == DocumentSortOption.QUIZ_COUNT) {
            documents = documentLocalizationRepository.findAllByMemberIdOrderByQuizCountDesc(memberId);
        } else {
            documents = documentLocalizationRepository.findAllOrderByWrongAnswerCount(memberId);
        }

        List<GetAllDocumentsResponse.GetAllDocumentsDocumentDto> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            int reviewNeededQuizCount = 0;
            List<Quiz> quizzes = new ArrayList<>(document.getQuizzes());
            quizzes.sort(Comparator.comparing(Quiz::getId).reversed());
            for (Quiz quiz : quizzes) {
                boolean reviewNeeded = quiz.isReviewNeeded();
                if (reviewNeeded) {
                    reviewNeededQuizCount += 1;
                }
            }

            String previewContent = "";

            if (!quizzes.isEmpty()) {
                previewContent = quizzes.getFirst().getQuestion();
            }

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
}

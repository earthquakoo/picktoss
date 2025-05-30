package com.picktoss.picktossserver.domain.document.dto.response;

import com.picktoss.picktossserver.global.enums.document.DocumentType;
import com.picktoss.picktossserver.global.enums.document.QuizGenerationStatus;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetSingleDocumentResponse {

    private Long id;
    private String name;
    private String emoji;
    private String content;
    private String category;
    private Boolean isPublic;
    private int bookmarkCount;
    private int characterCount;
    private int totalQuizCount;
    private LocalDateTime createdAt;
    private DocumentType documentType;
    private QuizGenerationStatus quizGenerationStatus;
    private List<GetSingleDocumentQuizDto> quizzes;

    @Getter
    @Builder
    public static class GetSingleDocumentQuizDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
        private boolean isReviewNeeded;
    }
}

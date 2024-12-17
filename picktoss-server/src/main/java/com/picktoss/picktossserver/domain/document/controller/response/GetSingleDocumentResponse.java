package com.picktoss.picktossserver.domain.document.controller.response;

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
    private String documentName;
    private QuizGenerationStatus quizGenerationStatus;
    private String content;
    private int characterCount;
    private int totalQuizCount;
    private LocalDateTime updatedAt;
    private GetSingleDocumentDirectoryDto directory;
    private List<GetSingleDocumentQuizDto> quizzes;

    @Getter
    @Builder
    public static class GetSingleDocumentDirectoryDto {
        private Long id;
        private String name;
        private String emoji;
    }

    @Getter
    @Builder
    public static class GetSingleDocumentQuizDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
    }
}

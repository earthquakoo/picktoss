package com.picktoss.picktossserver.domain.document.dto.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GetPublicSingleDocumentResponse {

    private Long id;
    private String creator;
    private String name;
    private String emoji;
    private String category;
    private int tryCount;
    private int bookmarkCount;
    private int totalQuizCount;
    private Boolean isBookmarked;
    private LocalDateTime createdAt;
    private List<GetPublicSingleDocumentQuizDto> quizzes;

    @Getter
    @Builder
    public static class GetPublicSingleDocumentQuizDto {
        private Long id;
        private String answer;
        private String question;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
    }
}

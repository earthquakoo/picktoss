package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuizSetByDocumentResponse {

    private List<GetQuizSetByDocumentQuizDto> quizzes;

    @Getter
    @Builder
    public static class GetQuizSetByDocumentQuizDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
        private GetQuizSetByDocumentDocumentDto document;
        private GetQuizSetByDocumentDirectoryDto directory;
    }

    @Getter
    @Builder
    public static class GetQuizSetByDocumentDocumentDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class GetQuizSetByDocumentDirectoryDto {
        private Long id;
        private String name;
    }
}

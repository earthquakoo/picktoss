package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuizSetResponse {

    private List<GetQuizSetQuizDto> quizzes;

    @Getter
    @Builder
    public static class GetQuizSetQuizDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
        private GetQuizSetDocumentDto document;
        private GetQuizSetDirectoryDto directory;
    }

    @Getter
    @Builder
    public static class GetQuizSetDocumentDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class GetQuizSetDirectoryDto {
        private Long id;
        private String name;
    }
}

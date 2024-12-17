package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllQuizzesByDirectoryIdResponse {

    private List<GetAllQuizzesByDirectoryQuizDto> quizzes;

    @Getter
    @Builder
    public static class GetAllQuizzesByDirectoryQuizDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
        private DocumentDto document;
    }

    @Getter
    @Builder
    public static class DocumentDto {
        private Long id;
        private String name;
    }
}

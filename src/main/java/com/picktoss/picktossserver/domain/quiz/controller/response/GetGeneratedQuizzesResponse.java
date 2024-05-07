package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetGeneratedQuizzesResponse {

    private List<GetGeneratedQuizzesQuizDto> quizzes;

    @Getter
    @Builder
    public static class GetGeneratedQuizzesQuizDto {
        private Long id;
        private String question;
        private String answer;
        private List<String> options;
        private QuizType quizType;
        private GetGeneratedQuizzesDocumentDto document;
        private GetGeneratedQuizzesCategoryDto category;
    }

    @Getter
    @Builder
    public static class GetGeneratedQuizzesDocumentDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class GetGeneratedQuizzesCategoryDto {
        private Long id;
        private String name;
    }
}

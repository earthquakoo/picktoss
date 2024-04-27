package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetSingleQuizResponse {

    private Long id;
    private String question;
    private String answer;
    private List<String> options;
    private String explanation;
    private QuizType quizType;
    private GetSingleQuizCategoryDto category;
    private GetSingleQuizDocumentDto document;

    @Getter
    @Builder
    public static class GetSingleQuizDocumentDto {
        private Long id;
        private String name;
    }


    @Getter
    @Builder
    public static class GetSingleQuizCategoryDto {
        private Long id;
        private String name;
    }
}

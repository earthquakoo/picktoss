package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class GetQuizSetResponse {

    private List<GetQuizSetQuizDto> quizSets;

    @Getter
    @Builder
    public static class GetQuizSetQuizDto {
        private Long id;
        private String question;
        private String answer;
        private List<String> options;
        private QuizType quizType;
        private GetQuizSetDocumentDto document;
        private GetQuizSetCategoryDto category;
    }

    @Getter
    @Builder
    public static class GetQuizSetDocumentDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class GetQuizSetCategoryDto {
        private Long id;
        private String name;
    }
}

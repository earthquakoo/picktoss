package com.picktoss.picktossserver.domain.quiz.controller.dto;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class QuizResponseDto {

    private List<QuizDto> quizzes;

    @Getter
    @Builder
    public static class QuizDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
        private DocumentDto document;
        private CategoryDto category;
    }

    @Getter
    @Builder
    public static class DocumentDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class CategoryDto {
        private Long id;
        private String name;
    }
}

package com.picktoss.picktossserver.domain.question.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuestionSetResponse {
    private List<QuestionDto> questions;

    @Getter
    @Builder
    public static class QuestionDto {
        private Long id;
        private String question;
        private String answer;
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

package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetBookmarkQuizResponse {

    private List<GetBookmarkQuizDto> quizzes;

    @Getter
    @Builder
    public static class GetBookmarkQuizDto {
        private Long id;
        private String question;
        private String answer;
        private List<String> options;
        private QuizType quizType;
        private GetBookmarkDocumentDto document;
        private GetBookmarkCategoryDto category;
    }

    @Getter
    @Builder
    public static class GetBookmarkDocumentDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class GetBookmarkCategoryDto {
        private Long id;
        private String name;
    }
}

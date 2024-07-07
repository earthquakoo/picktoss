package com.picktoss.picktossserver.domain.quiz.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetExampleQuizSetResponse {

    private List<CreateExampleQuizDto> quizzes;

    @Getter
    @Builder
    public static class CreateExampleQuizDto {
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
    }
}

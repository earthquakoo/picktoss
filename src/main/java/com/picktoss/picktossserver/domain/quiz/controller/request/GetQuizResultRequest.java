package com.picktoss.picktossserver.domain.quiz.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GetQuizResultRequest {

    private String quizSetId;
    private List<GetQuizResultQuizDto> quizzes;

    @Getter
    public static class GetQuizResultQuizDto {
        private Long id;
        private boolean answer;
        private String categoryName;
    }
}

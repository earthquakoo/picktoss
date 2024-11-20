package com.picktoss.picktossserver.domain.quiz.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateQuizResultRequest {

    private String quizSetId;
    private List<UpdateQuizResultQuizDto> quizzes;

    @Getter
    public static class UpdateQuizResultQuizDto {
        private Long id;
        private boolean answer;
        private String choseAnswer;
        private int elapsedTime;
    }
}

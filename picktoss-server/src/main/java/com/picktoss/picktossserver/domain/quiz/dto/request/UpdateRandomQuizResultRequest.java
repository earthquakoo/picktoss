package com.picktoss.picktossserver.domain.quiz.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateRandomQuizResultRequest {

    private List<UpdateRandomQuizResultDto> quizzes;

    @Getter
    public static class UpdateRandomQuizResultDto {
        private Long id;
        private boolean answer;
    }
}

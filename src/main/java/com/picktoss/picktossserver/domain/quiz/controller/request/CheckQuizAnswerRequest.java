package com.picktoss.picktossserver.domain.quiz.controller.request;

import lombok.Getter;

@Getter
public class CheckQuizAnswerRequest {
    private Long quizId;
    private boolean answer;
}

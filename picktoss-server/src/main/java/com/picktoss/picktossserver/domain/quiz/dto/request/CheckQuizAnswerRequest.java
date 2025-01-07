package com.picktoss.picktossserver.domain.quiz.dto.request;

import lombok.Getter;

@Getter
public class CheckQuizAnswerRequest {
    private Long quizId;
    private boolean answer;
}

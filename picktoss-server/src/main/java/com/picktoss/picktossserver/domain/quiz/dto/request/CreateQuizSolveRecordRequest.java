package com.picktoss.picktossserver.domain.quiz.dto.request;

import lombok.Getter;

@Getter
public class CreateQuizSolveRecordRequest {
    private Long quizId;
    private Boolean isAnswer;
    private String choseAnswer;
}

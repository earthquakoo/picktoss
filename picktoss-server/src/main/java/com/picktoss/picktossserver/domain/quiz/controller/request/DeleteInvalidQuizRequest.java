package com.picktoss.picktossserver.domain.quiz.controller.request;

import com.picktoss.picktossserver.global.enums.quiz.QuizErrorType;
import lombok.Getter;

@Getter
public class DeleteInvalidQuizRequest {
    private QuizErrorType quizErrorType;
}

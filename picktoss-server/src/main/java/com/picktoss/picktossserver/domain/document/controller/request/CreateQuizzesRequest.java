package com.picktoss.picktossserver.domain.document.controller.request;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.Getter;

@Getter
public class CreateQuizzesRequest {
    private int star;
    private QuizType quizType;
}

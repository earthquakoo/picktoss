package com.picktoss.picktossserver.domain.quiz.controller.request;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.Getter;

@Getter
public class CreateQuizzesRequest {
    private QuizType quizType;
    private int quizCount;
}

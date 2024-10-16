package com.picktoss.picktossserver.domain.quiz.controller.request;

import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateQuizzesRequest {
    private List<Long> documentIds;
    private int point;
    private QuizType quizType;
}

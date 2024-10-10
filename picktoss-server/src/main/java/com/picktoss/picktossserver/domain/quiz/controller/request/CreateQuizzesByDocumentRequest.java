package com.picktoss.picktossserver.domain.quiz.controller.request;

import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.Getter;

@Getter
public class CreateQuizzesByDocumentRequest {

    private QuizType quizType;
    private Integer quizCount;
}

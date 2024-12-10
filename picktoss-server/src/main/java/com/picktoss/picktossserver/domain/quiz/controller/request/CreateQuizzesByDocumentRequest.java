package com.picktoss.picktossserver.domain.quiz.controller.request;

import lombok.Getter;

@Getter
public class CreateQuizzesByDocumentRequest {

    private String quizType;
    private Integer quizCount;
}

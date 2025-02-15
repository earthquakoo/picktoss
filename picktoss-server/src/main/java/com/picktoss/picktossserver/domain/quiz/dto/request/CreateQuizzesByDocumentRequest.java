package com.picktoss.picktossserver.domain.quiz.dto.request;

import lombok.Getter;

@Getter
public class CreateQuizzesByDocumentRequest {

    private String quizType;
    private Integer quizCount;
}

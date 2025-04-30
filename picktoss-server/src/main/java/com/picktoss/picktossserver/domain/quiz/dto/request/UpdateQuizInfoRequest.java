package com.picktoss.picktossserver.domain.quiz.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateQuizInfoRequest {
    private String question;
    private String answer;
    private String explanation;
    private List<String> options;
}

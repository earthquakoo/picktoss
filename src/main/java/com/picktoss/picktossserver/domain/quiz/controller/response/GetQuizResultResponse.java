package com.picktoss.picktossserver.domain.quiz.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuizResultResponse {

    private List<GetQuizResultCategoryDto> categories;

    @Getter
    @Builder
    public static class GetQuizResultCategoryDto {
        private String name;
        private int incorrectAnswerCount;
    }
}
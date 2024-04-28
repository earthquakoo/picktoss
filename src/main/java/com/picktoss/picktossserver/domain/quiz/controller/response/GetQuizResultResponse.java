package com.picktoss.picktossserver.domain.quiz.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuizResultResponse {

    List<GetQuizResultCategoryDto> categories;

    @Getter
    @Builder
    public static class GetQuizResultCategoryDto {
        private Long id;
        private String name;
    }
}

package com.picktoss.picktossserver.domain.question.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetQuestionSetTodayResponse {
    private String questionSetId;
    private String message;
}

package com.picktoss.picktossserver.domain.quiz.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetCurrentTodayQuizInfo {
    private int currentConsecutiveDays;
    private int maxConsecutiveDays;
}
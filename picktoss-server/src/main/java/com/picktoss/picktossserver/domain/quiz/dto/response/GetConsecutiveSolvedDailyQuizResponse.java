package com.picktoss.picktossserver.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetConsecutiveSolvedDailyQuizResponse {
    private int currentConsecutiveDays;
}

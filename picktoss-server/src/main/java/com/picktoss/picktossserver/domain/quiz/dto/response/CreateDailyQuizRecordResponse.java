package com.picktoss.picktossserver.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateDailyQuizRecordResponse {

    private int reward;
    private int todaySolvedDailyQuizCount;
    private int consecutiveSolvedDailyQuizDays;
}

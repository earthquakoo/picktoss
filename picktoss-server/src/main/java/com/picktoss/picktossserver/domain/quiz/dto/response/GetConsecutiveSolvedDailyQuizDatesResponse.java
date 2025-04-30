package com.picktoss.picktossserver.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetConsecutiveSolvedDailyQuizDatesResponse {
    private List<GetDailyQuizRecordByDateDto> solvedDailyQuizDateRecords;

    @Getter
    @Builder
    public static class GetDailyQuizRecordByDateDto {
        private LocalDate date;
        private Boolean isDailyQuizComplete;
    }
}

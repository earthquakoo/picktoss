package com.picktoss.picktossserver.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetConsecutiveSolvedQuizSetDatesResponse {
    private List<GetQuizRecordByDateDto> solvedQuizDateRecords;

    @Getter
    @Builder
    public static class GetQuizRecordByDateDto {
        private LocalDate date;
        private Boolean isSolved;
    }
}

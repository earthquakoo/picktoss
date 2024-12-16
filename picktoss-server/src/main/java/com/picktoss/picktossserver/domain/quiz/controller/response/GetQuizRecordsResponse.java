package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuizRecordsResponse {

    private int currentConsecutiveDays;
    private int maxConsecutiveDays;
    private List<GetQuizRecordsSolvedDateDto> quizRecords;

    @Getter
    @Builder
    public static class GetQuizRecordsSolvedDateDto {
        private LocalDate solvedDate;
        private List<GetQuizRecordsDto> quizRecords;
    }

    @Getter
    @Builder
    public static class GetQuizRecordsDto {
        private String quizSetId;
        private String name;
        private int quizCount;
        private int score;
        private QuizSetType quizSetType;
    }
}

package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuizRecordResponse {

    private int currentConsecutiveDays;
    private int maxConsecutiveDays;
    private List<GetQuizRecordSolvedDateDto> quizRecords;

    @Getter
    @Builder
    public static class GetQuizRecordSolvedDateDto {
        private LocalDate solvedDate;
        private List<GetQuizRecordDto> quizRecords;
    }

    @Getter
    @Builder
    public static class GetQuizRecordDto {
        private String quizSetId;
        private String name;
        private int quizCount;
        private int score;
        private QuizSetType quizSetType;
    }
}

package com.picktoss.picktossserver.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetQuizWeeklyAnalysisResponse {
    private List<QuizAnswerRateAnalysisDto> quizzes;
    private List<QuizAnswerRateWeeklyAnalysisCategoryDto> categories;
    private QuizAnswerRateWeeklyAnalysisQuizTypeDto quizTypeDto;
    private double averageCorrectRate; // 평균 정답률
    private int averageDailyQuizCount; // 주간 평균 퀴즈 개수
    private int weeklyTotalQuizCount; // 주간동안 푼 총 퀴즈 개수


    @Getter
    @Builder
    public static class QuizAnswerRateAnalysisDto {
        private LocalDate date;
        private DayOfWeek dayOfWeek;
        private int totalQuizCount;
        private int correctAnswerCount;
    }

    @Getter
    @Builder
    public static class QuizAnswerRateWeeklyAnalysisCategoryDto {
        private String categoryName;
        private int totalQuizCount;
    }

    @Getter
    @Builder
    public static class QuizAnswerRateWeeklyAnalysisQuizTypeDto {
        private int multipleChoiceQuizCount;
        private int mixUpQuizCount;
    }
}

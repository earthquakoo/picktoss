package com.picktoss.picktossserver.domain.quiz.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetQuizMonthlyAnalysisResponse {

    private List<QuizAnswerRateAnalysisDto> quizzes;
    private List<QuizAnswerRateMonthlyAnalysisCategoryDto> categories;
    private QuizAnswerRateMonthlyAnalysisQuizTypeDto quizTypes;
    private double averageCorrectAnswerRate; // 평균 정답률
    private int maxSolvedQuizCount; // 가장 퀴즈를 많이 푼 날의 개수
    private int monthlyTotalQuizCount; // 한 달간 푼 퀴즈 개수

    @Getter
    @Builder
    public static class QuizAnswerRateAnalysisDto {
        private LocalDate date;
        private int totalQuizCount;
        private int correctAnswerCount;
    }

    @Getter
    @Builder
    public static class QuizAnswerRateMonthlyAnalysisCategoryDto {
        private String categoryName;
        private int totalQuizCount;
    }

    @Getter
    @Builder
    public static class QuizAnswerRateMonthlyAnalysisQuizTypeDto {
        private int multipleChoiceQuizCount;
        private int mixUpQuizCount;
    }
}

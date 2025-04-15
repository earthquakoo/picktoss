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
    private int monthlyTotalQuizCount; // 한 달간 푼 퀴즈 개수
    private int monthlyTotalCorrectQuizCount; // 한 달간 맞춘 정답 수
    private double averageCorrectAnswerRate; // 평균 정답률
    private int quizCountDifferenceFromLastMonth; // 전월 대비 퀴즈 개수 차이

    @Getter
    @Builder
    public static class QuizAnswerRateAnalysisDto {
        private LocalDate date;
        private int totalQuizCount;
        private int correctAnswerCount;
    }
}

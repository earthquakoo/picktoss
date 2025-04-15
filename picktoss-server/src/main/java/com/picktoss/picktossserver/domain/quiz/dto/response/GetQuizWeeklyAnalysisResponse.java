package com.picktoss.picktossserver.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetQuizWeeklyAnalysisResponse {
    private List<QuizAnswerRateAnalysisDto> quizzes;
    private int averageDailyQuizCount; // 하루에 평균적으로 푼 퀴즈 개수
    private double averageCorrectRate; // 평균 정답률
    private int weeklyTotalQuizCount; // 주간동안 푼 총 퀴즈 개수


    @Getter
    @Builder
    public static class QuizAnswerRateAnalysisDto {
        private LocalDate date;
        private int totalQuizCount;
        private int correctAnswerCount;
    }
}

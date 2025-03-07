package com.picktoss.picktossserver.domain.quiz.dto.response;

import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class GetQuizWeeklyAnalysisResponse {
    private List<QuizAnswerRateAnalysisDto> quizzes;
    private int averageDailyQuizCount; // 하루에 평균적으로 푼 퀴즈 개수
    private double averageCorrectRate; // 평균 정답률
    private int weeklyTotalQuizCount; // 주간동안 푼 총 퀴즈 개수
    private Map<CollectionCategory, Integer> collectionsAnalysis;


    @Getter
    @Builder
    public static class QuizAnswerRateAnalysisDto {
        private LocalDate date;
        private int totalQuizCount;
        private int correctAnswerCount;
    }
}

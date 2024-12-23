package com.picktoss.picktossserver.domain.quiz.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetQuizAnswerRateAnalysisResponse {

    private List<QuizAnswerRateAnalysisDto> quizzes;

    @Getter
    @Builder
    public static class QuizAnswerRateAnalysisDto {
        private LocalDate date;
        private int totalQuizCount;
        private int incorrectAnswerCount;
    }
}

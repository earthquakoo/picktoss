package com.picktoss.picktossserver.domain.quiz.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class GetQuizAnalysisResponse {
    private int totalQuizCount;
    private int mixUpQuizCount;
    private int multipleQuizCount;
    private int incorrectAnswerCount;
    private LocalTime elapsedTime;
}

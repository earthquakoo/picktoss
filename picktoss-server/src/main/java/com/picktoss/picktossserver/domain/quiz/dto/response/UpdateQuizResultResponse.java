package com.picktoss.picktossserver.domain.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateQuizResultResponse {

    private int totalQuizCount;
    private int totalElapsedTime;
    private double correctAnswerRate;
}

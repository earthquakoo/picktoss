package com.picktoss.picktossserver.domain.quiz.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(type = "string", example = "HH:mm:ss")
    private String elapsedTime;
}

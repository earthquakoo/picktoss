package com.picktoss.picktossserver.domain.quiz.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;

@Getter
@NoArgsConstructor
public class GetQuizResultRequest {

    private String quizSetId;
    private List<GetQuizResultQuizDto> quizzes;

    @Getter
    public static class GetQuizResultQuizDto {
        private Long id;
        private boolean answer;
        @Schema(type = "string", example = "HH:mm:ss")
        private String elapsedTime;
    }
}

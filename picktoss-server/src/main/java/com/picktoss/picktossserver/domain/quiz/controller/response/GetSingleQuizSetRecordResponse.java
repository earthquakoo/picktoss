package com.picktoss.picktossserver.domain.quiz.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetSingleQuizSetRecordResponse {

    private LocalDateTime createdAt;
    private Integer elapsedTime;
    private List<GetSingleQuizSetRecordDto> quizzes;

    @Getter
    @Builder
    public static class GetSingleQuizSetRecordDto {
        private String question;
        private String answer;
        private String explanation;
        private boolean isAnswer;
        private String choseAnswer;
        private String documentName;
        private String categoryName;
    }
}

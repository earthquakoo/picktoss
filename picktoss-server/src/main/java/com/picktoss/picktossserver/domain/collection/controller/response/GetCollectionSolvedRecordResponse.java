package com.picktoss.picktossserver.domain.collection.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetCollectionSolvedRecordResponse {

    private LocalDateTime createdAt;
    private Integer elapsedTime;
    private List<GetCollectionSolvedRecordDto> quizzes;

    @Getter
    @Builder
    public static class GetCollectionSolvedRecordDto {
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private boolean isAnswer;
        private String choseAnswer;
    }
}

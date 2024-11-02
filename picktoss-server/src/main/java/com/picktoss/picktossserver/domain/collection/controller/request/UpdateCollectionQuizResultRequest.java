package com.picktoss.picktossserver.domain.collection.controller.request;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateCollectionQuizResultRequest {

    private List<UpdateCollectionQuizResultDto> collectionQuizzes;

    @Getter
    public static class UpdateCollectionQuizResultDto {
        private Long quizId;
        private Integer elapsedTimeMs;
        private Boolean isAnswer;
        private String choseAnswer;
    }
}

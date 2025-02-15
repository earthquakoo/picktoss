package com.picktoss.picktossserver.domain.collection.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateCollectionQuizResultRequest {

    private String quizSetId;
    private List<UpdateCollectionQuizResultQuizDto> quizzes;

    @Getter
    public static class UpdateCollectionQuizResultQuizDto {
        private Long id;
        private boolean answer;
        private String choseAnswer;
        private int elapsedTime;
    }
}

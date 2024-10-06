package com.picktoss.picktossserver.domain.collection.controller.response;

import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetSingleCollectionResponse {

    private String name;
    private String description;
    private String tag;
    private int solvedCount;
    private int bookmarkCount;
    private List<GetSingleCollectionQuizDto> quizzes;


    @Getter
    @Builder
    public static class GetSingleCollectionQuizDto {
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
    }
}

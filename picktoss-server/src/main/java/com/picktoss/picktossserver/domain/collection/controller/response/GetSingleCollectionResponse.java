package com.picktoss.picktossserver.domain.collection.controller.response;

import com.picktoss.picktossserver.global.enums.collection.CollectionField;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetSingleCollectionResponse {

    private Long id;
    private String name;
    private String description;
    private String emoji;
    private int bookmarkCount;
    private CollectionField collectionField;
    private String createMemberName;
    private int solvedMemberCount;
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

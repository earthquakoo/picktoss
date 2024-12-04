package com.picktoss.picktossserver.domain.collection.controller.response;

import com.picktoss.picktossserver.global.enums.collection.CollectionField;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllMyCollectionsResponse {

    private List<GetAllMyCollectionsDto> collections;

    @Getter
    @Builder
    public static class GetAllMyCollectionsDto {
        private Long id;
        private String name;
        private String description;
        private String emoji;
        private int bookmarkCount;
        private CollectionField collectionField;
        private String createMemberName;
        private int solvedMemberCount;
        private List<GetAllMyCollectionsQuizDto> quizzes;
    }

    @Getter
    @Builder
    public static class GetAllMyCollectionsQuizDto {
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
    }
}

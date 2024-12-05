package com.picktoss.picktossserver.domain.collection.controller.response;

import com.picktoss.picktossserver.global.enums.collection.CollectionField;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllCollectionsResponse {

    private List<GetAllCollectionsDto> collections;

    @Getter
    @Builder
    public static class GetAllCollectionsDto {
        private Long id;
        private String name;
        private String description;
        private String emoji;
        private int bookmarkCount;
        private CollectionField collectionField;
        private int solvedMemberCount;
        private GetAllCollectionsMemberDto member;
        private List<GetAllCollectionsQuizDto> quizzes;
    }

    @Getter
    @Builder
    public static class GetAllCollectionsQuizDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
    }

    @Getter
    @Builder
    public static class GetAllCollectionsMemberDto {
        private Long creatorId;
        private String creatorName;
    }
}

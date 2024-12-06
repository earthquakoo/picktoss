package com.picktoss.picktossserver.domain.collection.controller.dto;

import com.picktoss.picktossserver.global.enums.collection.CollectionField;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CollectionResponseDto {

    private List<CollectionQuizDto> collections;

    @Getter
    @Builder
    public static class CollectionQuizDto {
        private Long id;
        private String name;
        private String description;
        private String emoji;
        private int bookmarkCount;
        private CollectionField collectionField;
        private int solvedMemberCount;
        private boolean isBookmarked;
        private CollectionMemberDto member;
        private List<CollectionQuizzesDto> quizzes;
    }

    @Getter
    @Builder
    public static class CollectionQuizzesDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
    }

    @Getter
    @Builder
    public static class CollectionMemberDto {
        private Long creatorId;
        private String creatorName;
    }
}

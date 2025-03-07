package com.picktoss.picktossserver.domain.collection.dto.response;

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
    private String collectionCategory;
    private int solvedMemberCount;
    private boolean bookmarked;
    private GetSingleCollectionMemberDto member;
    private List<GetSingleCollectionQuizDto> quizzes;

    @Getter
    @Builder
    public static class GetSingleCollectionQuizDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
    }

    @Getter
    @Builder
    public static class GetSingleCollectionMemberDto {
        private Long creatorId;
        private String creatorName;
    }
}

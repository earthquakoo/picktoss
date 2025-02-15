package com.picktoss.picktossserver.domain.collection.dto.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuizzesInCollectionByCollectionCategory {

    private List<QuizInCollectionDto> quizzes;

    @Getter
    @Builder
    public static class QuizInCollectionDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
        private QuizInCollectionByCollectionDto collection;
    }

    @Getter
    @Builder
    public static class QuizInCollectionByCollectionDto {
        private Long id;
        private String name;
    }
}

package com.picktoss.picktossserver.domain.collection.controller.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetQuizzesInCollectionByCollectionField {

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
    }
}

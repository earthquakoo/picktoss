package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetDocumentsNeedingReviewPickResponse {

    private List<GetReviewQuizDto> quizzes;

    @Getter
    @Builder
    public static class GetReviewQuizDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private List<String> options;
        private QuizType quizType;
        private String description;
        private String choseAnswer;
    }
}

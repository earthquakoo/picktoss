package com.picktoss.picktossserver.domain.quiz.dto.request;

import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateQuizResultRequest {

    private String quizSetId;
    private QuizSetType quizSetType;
    private List<UpdateQuizResultQuizDto> quizzes;

    @Getter
    public static class UpdateQuizResultQuizDto {
        private Long id;
        private boolean answer;
        private String choseAnswer;
        private int elapsedTime;
    }
}

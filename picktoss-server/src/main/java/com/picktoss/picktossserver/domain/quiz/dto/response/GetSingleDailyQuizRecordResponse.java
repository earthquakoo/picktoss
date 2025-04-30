package com.picktoss.picktossserver.domain.quiz.dto.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetSingleDailyQuizRecordResponse {

    private List<GetSingleDailyQuizRecordDto> quizzes;

    @Getter
    @Builder
    public static class GetSingleDailyQuizRecordDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private QuizType quizType;
        private List<String> options;
        private Boolean isAnswer;
        private String choseAnswer;
    }
}

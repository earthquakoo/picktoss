package com.picktoss.picktossserver.domain.quiz.controller.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetSingleQuizSetRecordResponse {

    private Integer totalElapsedTimeMs;
    private List<GetSingleQuizSetRecordDto> quizzes;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class GetSingleQuizSetRecordDto {
        private Long id;
        private String question;
        private String answer;
        private String explanation;
        private QuizType quizType;
        private List<String> options;
        private boolean isAnswer;
        private String choseAnswer;
        private String documentName;
        private String directoryName;
        private String collectionName;
        private QuizSetType quizSetType;
    }
}

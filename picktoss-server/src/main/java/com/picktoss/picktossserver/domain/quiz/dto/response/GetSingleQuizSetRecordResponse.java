package com.picktoss.picktossserver.domain.quiz.dto.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetSingleQuizSetRecordResponse {

    private String name;
    private String emoji;
    private Integer totalQuizCount;
    private Integer totalElapsedTimeMs;
    private double averageCorrectAnswerRate; // 정답률
    private LocalDateTime createdAt;
    private List<GetSingleQuizSetRecordDto> quizzes;

    @Getter
    @Builder
    public static class GetSingleQuizSetRecordDto {
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

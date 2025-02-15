package com.picktoss.picktossserver.domain.quiz.dto.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreateQuizzesResponse {
    private String quizSetId;
    private QuizSetType quizSetType;
    private LocalDateTime createdAt;
}

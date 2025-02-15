package com.picktoss.picktossserver.domain.quiz.dto.response;

import com.picktoss.picktossserver.global.enums.quiz.QuizSetResponseType;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class GetQuizSetTodayResponse {

    private String quizSetId;
    private QuizSetType quizSetType;
    private QuizSetResponseType type;
    private LocalDateTime createdAt;
}

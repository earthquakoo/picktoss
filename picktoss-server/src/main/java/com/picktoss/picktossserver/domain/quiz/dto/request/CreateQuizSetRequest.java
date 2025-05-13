package com.picktoss.picktossserver.domain.quiz.dto.request;

import com.picktoss.picktossserver.global.enums.quiz.DailyQuizType;
import lombok.Getter;

@Getter
public class CreateQuizSetRequest {
    private Integer quizCount;
    private DailyQuizType quizType;
}

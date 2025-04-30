package com.picktoss.picktossserver.domain.quiz.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateQuizSetResponse {
    private Long quizSetId;
}

package com.picktoss.picktossserver.domain.quiz.controller.response;

import jakarta.annotation.security.DenyAll;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateQuizResultResponse {
    private Integer reward;
}

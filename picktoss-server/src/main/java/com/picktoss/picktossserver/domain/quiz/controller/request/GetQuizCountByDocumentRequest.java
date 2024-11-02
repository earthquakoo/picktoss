package com.picktoss.picktossserver.domain.quiz.controller.request;

import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.Getter;

import java.util.List;

@Getter
public class GetQuizCountByDocumentRequest {
    private List<Long> documentIds;
    private QuizType type;
}

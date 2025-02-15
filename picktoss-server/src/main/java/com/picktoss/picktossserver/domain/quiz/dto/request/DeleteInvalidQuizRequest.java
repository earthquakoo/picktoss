package com.picktoss.picktossserver.domain.quiz.dto.request;

import com.picktoss.picktossserver.global.enums.quiz.QuizErrorType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class DeleteInvalidQuizRequest {
    @Schema(implementation = QuizErrorType.class, example = "CHOICE_OR_QUESTION_MISSING, QUIZ_TYPE_MISMATCH, UNRELATED_QUIZ")
    private QuizErrorType quizErrorType;
}

package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.quiz.dto.request.DeleteQuizRequest;
import com.picktoss.picktossserver.domain.quiz.service.QuizDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_NOT_FOUND_ERROR;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizDeleteController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizDeleteService quizDeleteService;

    @Operation(summary = "퀴즈 삭제")
    @DeleteMapping("/quizzes/delete")
    @ApiErrorCodeExample(QUIZ_NOT_FOUND_ERROR)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuiz(
            @Valid @RequestBody DeleteQuizRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizDeleteService.deleteQuiz(request.getId(), memberId);
    }
}

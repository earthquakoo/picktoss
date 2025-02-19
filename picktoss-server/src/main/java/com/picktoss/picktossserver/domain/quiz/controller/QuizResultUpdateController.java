package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateRandomQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.dto.response.UpdateQuizResultResponse;
import com.picktoss.picktossserver.domain.quiz.service.QuizResultUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_SET_NOT_FOUND_ERROR;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizResultUpdateController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizResultUpdateService quizResultUpdateService;

    @Operation(summary = "퀴즈 결과 업데이트")
    @PatchMapping("/quiz/result")
    @ApiErrorCodeExample(QUIZ_SET_NOT_FOUND_ERROR)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UpdateQuizResultResponse> updateQuizResult(@Valid @RequestBody UpdateQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        UpdateQuizResultResponse response = quizResultUpdateService.updateQuizResult(request.getQuizzes(), request.getQuizSetId(), request.getQuizSetType(), memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "랜덤 퀴즈 결과 업데이트")
    @PatchMapping("/random-quiz/result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRandomQuizResult(@Valid @RequestBody UpdateRandomQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizResultUpdateService.updateRandomQuizResult(request.getQuizzes(), memberId);
    }

    @Operation(summary = "오답 터뜨리기 결과 업데이트")
    @PatchMapping("/wrong-quiz/result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateWrongQuizResult(@Valid @RequestBody UpdateRandomQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizResultUpdateService.updateWrongQuizResult(request.getQuizzes(), memberId);
    }
}

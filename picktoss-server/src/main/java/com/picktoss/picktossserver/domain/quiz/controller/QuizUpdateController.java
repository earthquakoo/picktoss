package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateQuizInfoRequest;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.dto.response.UpdateQuizResultResponse;
import com.picktoss.picktossserver.domain.quiz.service.QuizUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizUpdateController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizUpdateService quizUpdateService;

    @Operation(summary = "퀴즈 세트 결과 업데이트")
    @PatchMapping("/documents/{document_id}/quiz-sets/{quiz_set_id}/update-result")
    @ApiErrorCodeExamples({QUIZ_SET_NOT_FOUND_ERROR, DOCUMENT_NOT_FOUND})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UpdateQuizResultResponse> updateQuizResult(
            @PathVariable("document_id") Long documentId,
            @PathVariable("quiz_set_id") Long quizSetId,
            @Valid @RequestBody UpdateQuizResultRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        UpdateQuizResultResponse response = quizUpdateService.updateQuizResult(request.getQuizzes(), quizSetId, memberId, documentId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 정보 변경")
    @PatchMapping("/quizzes/{quiz_id}/update-info")
    @ApiErrorCodeExample(QUIZ_NOT_FOUND_ERROR)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQuizInfo(
            @PathVariable("quiz_id") Long quizId,
            @Valid @RequestBody UpdateQuizInfoRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizUpdateService.updateQuizInfo(quizId, memberId, request.getQuestion(), request.getAnswer(), request.getExplanation(), request.getOptions());
    }

    @Operation(summary = "퀴즈 오답 확인(이해했습니다)")
    @PatchMapping("/quizzes/{quiz_id}/wrong-answer-confirm")
    @ApiErrorCodeExample(QUIZ_NOT_FOUND_ERROR)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateWrongAnswerConfirm(
            @PathVariable("quiz_id") Long quizId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizUpdateService.updateIsReviewNeededByWrongAnswerQuizConfirm(quizId, memberId);
    }
}

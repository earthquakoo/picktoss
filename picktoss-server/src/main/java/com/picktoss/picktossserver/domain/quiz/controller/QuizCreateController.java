package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.quiz.dto.request.CreateQuizzesByDocumentRequest;
import com.picktoss.picktossserver.domain.quiz.dto.response.CreateQuizzesResponse;
import com.picktoss.picktossserver.domain.quiz.service.QuizCreateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizCreateController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizCreateService quizCreateService;

    @Operation(summary = "사용자가 생성한 문서에서 직접 퀴즈 세트 생성(랜덤, OX, 객관식)")
    @PostMapping("/quizzes/documents/{document_id}/custom-quiz-set")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, QUIZ_COUNT_EXCEEDED, QUIZ_TYPE_NOT_IN_DOCUMENT})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateQuizzesResponse> createMemberGeneratedQuizSet(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody CreateQuizzesByDocumentRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        CreateQuizzesResponse response = quizCreateService.createMemberGeneratedQuizSet(documentId, memberId, request.getQuizType(), request.getQuizCount());
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 생성 후 퀴즈 오류 확인을 위한 퀴즈세트 생성(퀴즈 시작하기 후 모든 퀴즈 생성이 완료되면 요청)")
    @PostMapping("/quizzes/documents/{document_id}/check-quiz-set")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateQuizzesResponse> createErrorCheckQuizSet(
            @PathVariable("document_id") Long documentId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        CreateQuizzesResponse response = quizCreateService.createErrorCheckQuizSet(documentId, memberId);
        return ResponseEntity.ok().body(response);
    }
}

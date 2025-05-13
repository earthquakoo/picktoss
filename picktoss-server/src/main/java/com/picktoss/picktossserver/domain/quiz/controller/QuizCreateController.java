package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.quiz.dto.request.CreateQuizSetRequest;
import com.picktoss.picktossserver.domain.quiz.dto.response.CreateQuizSetResponse;
import com.picktoss.picktossserver.domain.quiz.service.QuizCreateService;
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
public class QuizCreateController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizCreateService quizCreateService;

    @Operation(summary = "퀴즈 시작하기")
    @PostMapping("/documents/{document_id}/quiz-sets")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, QUIZ_COUNT_EXCEEDED})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateQuizSetResponse> createPublicDocumentQuizSet(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody CreateQuizSetRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        CreateQuizSetResponse response = quizCreateService.createQuizSet(documentId, memberId, request.getQuizCount(), request.getQuizType());
        return ResponseEntity.ok().body(response);
    }
}

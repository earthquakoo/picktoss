package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizSetResponse;
import com.picktoss.picktossserver.domain.quiz.service.QuizReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizReadController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizReadService quizReadService;

    @Operation(summary = "퀴즈 세트 가져오기")
    @GetMapping("/quiz-sets/{quiz_set_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetResponse> getQuizSets(
            @PathVariable("quiz_set_id") Long quizSetId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo != null ? jwtUserInfo.getMemberId() : null;

        GetQuizSetResponse response = quizReadService.findQuizSetByQuizSetId(quizSetId, memberId);
        return ResponseEntity.ok().body(response);
    }
}

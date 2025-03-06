package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizMonthlyAnalysisResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizWeeklyAnalysisResponse;
import com.picktoss.picktossserver.domain.quiz.service.QuizAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizAnalysisController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizAnalysisService quizAnalysisService;

    @Operation(summary = "퀴즈 주단위 분석")
    @GetMapping("/quizzes/analysis/weekly")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizWeeklyAnalysisResponse> getQuizWeeklyAnalysis(
            @RequestParam(required = false, value = "directory-id") Long directoryId,
            @RequestParam(required = false, value = "startDate") LocalDate startDate,
            @RequestParam(required = false, value = "endDate") LocalDate endDate
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizWeeklyAnalysisResponse response = quizAnalysisService.findQuizWeeklyAnalysis(memberId, directoryId, startDate, endDate);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 월단위 분석")
    @GetMapping("/quizzes/analysis/monthly")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizMonthlyAnalysisResponse> getQuizMonthlyAnalysis(
            @RequestParam(required = false, value = "directory-id") Long directoryId,
            @RequestParam(required = false, value = "month") LocalDate month
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizMonthlyAnalysisResponse response = quizAnalysisService.findQuizMonthlyAnalysis(memberId, directoryId, month);
        return ResponseEntity.ok().body(response);
    }
}

package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.quiz.dto.request.CreateQuizSolveRecordRequest;
import com.picktoss.picktossserver.domain.quiz.dto.response.CreateDailyQuizRecordResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetAllQuizzesResponse;
import com.picktoss.picktossserver.domain.quiz.service.DailyQuizRecordService;
import com.picktoss.picktossserver.global.enums.quiz.DailyQuizType;
import com.picktoss.picktossserver.global.enums.quiz.QuizSource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_NOT_FOUND_ERROR;

@Tag(name = "Quiz - Daily")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DailyQuizRecordController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DailyQuizRecordService dailyQuizRecordService;

    @Operation(summary = "데일리 퀴즈 가져오기")
    @GetMapping("/quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllQuizzesResponse> getQuizzes(
            @RequestParam(defaultValue = "ALL", value = "quiz-type") DailyQuizType dailyQuizType,
            @RequestParam(defaultValue = "ALL", value = "quiz-source") QuizSource quizSource
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String language = LocaleContextHolder.getLocale().getLanguage();

        GetAllQuizzesResponse response = dailyQuizRecordService.findQuizzes(memberId, dailyQuizType, quizSource, language);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "데일리 퀴즈 풀기")
    @PostMapping("/quizzes/solve")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, QUIZ_NOT_FOUND_ERROR})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateDailyQuizRecordResponse> createDailyQuizRecord(
            @Valid @RequestBody CreateQuizSolveRecordRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        ZoneId memberZoneId = LocaleContextHolder.getTimeZone().toZoneId();

        CreateDailyQuizRecordResponse response = dailyQuizRecordService.createDailyQuizRecord(memberId, request.getQuizId(), request.getChoseAnswer(), request.getIsAnswer(), memberZoneId);
        return ResponseEntity.ok().body(response);
    }
}

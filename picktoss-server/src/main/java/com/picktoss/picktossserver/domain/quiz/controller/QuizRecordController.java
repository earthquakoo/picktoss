package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.quiz.dto.response.*;
import com.picktoss.picktossserver.domain.quiz.service.QuizRecordService;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizRecordController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizRecordService quizRecordService;

    @Operation(summary = "전체 퀴즈 연속일 현황")
    @GetMapping("/quiz-set/consecutive-days")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetCurrentTodayQuizInfo> getCurrentConsecutiveSolvedQuizSet() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetCurrentTodayQuizInfo response = quizRecordService.findCurrentConsecutiveSolvedQuizSet(memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "월별 퀴즈 연속일 기록")
    @GetMapping("/quiz-set/{solved_date}/consecutive-days")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetConsecutiveSolvedQuizSetDatesResponse> getConsecutiveSolvedQuizSetDates(
            @PathVariable("solved_date") LocalDate solvedDate
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetConsecutiveSolvedQuizSetDatesResponse response = quizRecordService.findConsecutiveSolvedQuizSetDates(memberId, solvedDate);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "날짜별 퀴즈 기록")
    @GetMapping("/quizzes/{solved_date}/quiz-record")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleQuizRecordByDateResponse> getSingleQuizSetRecordByDate(
            @PathVariable("solved_date") LocalDate solvedDate
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleQuizRecordByDateResponse response = quizRecordService.findAllQuizSetRecordByDate(memberId, solvedDate);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "전체 퀴즈 기록")
    @GetMapping("/quizzes/quiz-records")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizRecordsResponse> getAllQuizzesAndCollectionRecords() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizRecordsResponse response = quizRecordService.findAllQuizAndCollectionRecords(memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 세트에 대한 상세 기록")
    @GetMapping("/quizzes/{quiz_set_id}/{quiz_set_type}/quiz-record")
    @ApiErrorCodeExamples({QUIZ_SET_NOT_FOUND_ERROR, UNRESOLVED_QUIZ_SET, QUIZ_SET_TYPE_ERROR})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleQuizSetRecordResponse> getSingleQuizSetRecord(
            @PathVariable("quiz_set_id") String quizSetId,
            @PathVariable("quiz_set_type") QuizSetType quizSetType
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleQuizSetRecordResponse response = quizRecordService.findQuizSetRecordByMemberIdAndQuizSetId(memberId, quizSetId, quizSetType);
        return ResponseEntity.ok().body(response);
    }
}

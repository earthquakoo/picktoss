package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.quiz.dto.response.*;
import com.picktoss.picktossserver.domain.quiz.service.QuizRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.UNRESOLVED_QUIZ_SET;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizRecordController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizRecordService quizRecordService;

    @Operation(summary = "전체 퀴즈 기록")
    @GetMapping("/quizzes/records")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllQuizRecordsResponse> getAllQuizzesAndCollectionRecords() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetAllQuizRecordsResponse response = quizRecordService.findAllQuizRecords(memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 세트에 대한 상세 기록")
    @GetMapping("/quiz-sets/{quiz_set_id}/record")
    @ApiErrorCodeExample(UNRESOLVED_QUIZ_SET)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleQuizSetRecordResponse> getSingleQuizSetRecord(
            @PathVariable("quiz_set_id") Long quizSetId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleQuizSetRecordResponse response = quizRecordService.findSingleQuizSetRecord(memberId, quizSetId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "데일리 퀴즈에 대한 상세 기록")
    @GetMapping("/daily-quiz-records/{daily_quiz_record_id}/record")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleDailyQuizRecordResponse> getSingleDailyQuizRecord(
            @PathVariable("daily_quiz_record_id") Long dailyQuizRecordId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleDailyQuizRecordResponse response = quizRecordService.findSingleDailyQuizRecord(dailyQuizRecordId, memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "월별 데일리 퀴즈 연속일 기록")
    @GetMapping("/daily-quiz-records/{solved_date}/consecutive-days")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetConsecutiveSolvedDailyQuizDatesResponse> getConsecutiveSolvedQuizSetDates(
            @PathVariable("solved_date") LocalDate solvedDate
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetConsecutiveSolvedDailyQuizDatesResponse response = quizRecordService.findConsecutiveSolvedQuizSetDates(memberId, solvedDate);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "데일리 퀴즈 연속일 현황")
    @GetMapping("/daily-quiz-records/consecutive-days")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetConsecutiveSolvedDailyQuizResponse> getConsecutiveSolvedDailyQuiz() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        int response = quizRecordService.calculateConsecutiveDailyQuiz(memberId);
        return ResponseEntity.ok().body(new GetConsecutiveSolvedDailyQuizResponse(response));
    }
}

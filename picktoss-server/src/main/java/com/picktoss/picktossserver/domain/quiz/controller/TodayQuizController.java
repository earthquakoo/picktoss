package com.picktoss.picktossserver.domain.quiz.controller;


import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetCurrentTodayQuizInfo;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizSetTodayResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetTodaySolvedQuizCountResponse;
import com.picktoss.picktossserver.domain.quiz.service.TodayQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class TodayQuizController {

    private final JwtTokenProvider jwtTokenProvider;
    private final TodayQuizService todayQuizService;

    @Operation(summary = "오늘의 퀴즈 세트 정보 가져오기")
    @GetMapping("/quiz-sets/today")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetTodayResponse> getQuizSetToday() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizSetTodayResponse quizSetToday = todayQuizService.findQuizSetToday(memberId);
        return ResponseEntity.ok().body(quizSetToday);
    }

    @Operation(summary = "오늘의 퀴즈 현황")
    @GetMapping("/today-quiz-info")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetCurrentTodayQuizInfo> getCurrentTodayQuizInfo() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetCurrentTodayQuizInfo response = todayQuizService.findCurrentTodayQuizInfo(memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "오늘 푼 퀴즈 수")
    @GetMapping("/quizzes/solved/today")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetTodaySolvedQuizCountResponse> getTodaySolvedQuizCount() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        int todaySolvedQuizCount = todayQuizService.findTodaySolvedQuizCount(memberId);
        return ResponseEntity.ok().body(new GetTodaySolvedQuizCountResponse(todaySolvedQuizCount));
    }
}

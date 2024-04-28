package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.quiz.controller.request.CheckQuizAnswerRequest;
import com.picktoss.picktossserver.domain.quiz.controller.request.GetQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.request.UpdateBookmarkQuizRequest;
import com.picktoss.picktossserver.domain.quiz.controller.response.*;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.facade.QuizFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "6. Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class QuizController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizFacade quizFacade;

    @Operation(summary = "Get single quiz")
    @GetMapping("/quiz/{quiz_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleQuizResponse> getSingleQuiz(@PathVariable("quiz_id") Long quizId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleQuizResponse response = quizFacade.findQuiz(quizId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Get quiz set")
    @GetMapping("/quiz-sets/{quiz_set_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetResponse> getQuizSet(@PathVariable("quiz_set_id") String quizSetId) {
        List<GetQuizSetResponse.GetQuizSetQuizDto> quizSets = quizFacade.findQuizSet(quizSetId);
        return ResponseEntity.ok().body(new GetQuizSetResponse(quizSets));
    }

    @Operation(summary = "Get quiz set today")
    @GetMapping("/quiz-sets/today")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetTodayResponse> getQuizSetToday() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizSetTodayResponse quizSetToday = quizFacade.findQuizSetToday(memberId);

        return ResponseEntity.ok().body(quizSetToday);
    }

    @Operation(summary = "Get bookmarked quiz")
    @GetMapping("/bookmark")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetBookmarkQuizResponse> getBookmarkedQuiz() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetBookmarkQuizResponse.GetBookmarkQuizDto> response = quizFacade.findBookmarkQuiz();
        return ResponseEntity.ok().body(new GetBookmarkQuizResponse(response));
    }

    @Operation(summary = "Update bookmarked quiz")
    @PatchMapping("/bookmark/{quiz_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBookmarkQuiz(
            @Valid @RequestBody UpdateBookmarkQuizRequest request,
            @PathVariable("quiz_id") Long quizId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizFacade.updateBookmarkQuiz(quizId, request.isBookmark());
    }

    @Operation(summary = "Get quiz result")
    @PostMapping("/quiz/result")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizResultResponse> getQuizResult(@Valid @RequestBody GetQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetQuizResultResponse.GetQuizResultCategoryDto> response = quizFacade.findQuizResult(request.getQuizSetId());
        return ResponseEntity.ok().body(new GetQuizResultResponse(response));
    }

    @Operation(summary = "Check quiz answer")
    @PatchMapping("/quiz/check-answer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkQuizAnswer(@Valid @RequestBody CheckQuizAnswerRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizFacade.checkQuizAnswer(request.getQuizId(), request.isAnswer());
    }
}

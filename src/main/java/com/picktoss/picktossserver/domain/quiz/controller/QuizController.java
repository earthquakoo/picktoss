package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.quiz.controller.dto.QuizResponseDto;
import com.picktoss.picktossserver.domain.quiz.controller.mapper.QuizMapper;
import com.picktoss.picktossserver.domain.quiz.controller.request.CheckQuizAnswerRequest;
import com.picktoss.picktossserver.domain.quiz.controller.request.CreateQuizzesRequest;
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

    @Operation(summary = "Get quiz set")
    @GetMapping("/quiz-sets/{quiz_set_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> getQuizSet(@PathVariable("quiz_set_id") String quizSetId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizFacade.findQuizSet(quizSetId, memberId);
        QuizResponseDto quizResponseDto = QuizMapper.quizzesToQuizResponseDto(quizzes);
        return ResponseEntity.ok().body(quizResponseDto);
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

    @Operation(summary = "Create quiz")
    @PostMapping("/quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> createQuizzes(@Valid @RequestBody CreateQuizzesRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizFacade.createQuizzes(request.getDocuments(), request.getPoint());
        QuizResponseDto quizResponseDto = QuizMapper.quizzesToQuizResponseDto(quizzes);
        return ResponseEntity.ok().body(quizResponseDto);
    }

    @Operation(summary = "Get all generated quizzes")
    @GetMapping("/quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> getGeneratedQuizzes() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizFacade.findAllGeneratedQuizzes(memberId);
        QuizResponseDto quizResponseDto = QuizMapper.quizzesToQuizResponseDto(quizzes);
        return ResponseEntity.ok().body(quizResponseDto);
    }

    @Operation(summary = "Get bookmarked quiz")
    @GetMapping("/bookmark")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> getBookmarkedQuiz() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizFacade.findBookmarkQuiz();
        QuizResponseDto quizResponseDto = QuizMapper.quizzesToQuizResponseDto(quizzes);
        return ResponseEntity.ok().body(quizResponseDto);
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

    @Operation(summary = "Update quiz result")
    @PatchMapping("/quiz/result")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizResultResponse> updateQuizResult(@Valid @RequestBody GetQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetQuizResultResponse.GetQuizResultCategoryDto> response = quizFacade.updateQuizResult(request.getQuizzes(), request.getQuizSetId(), memberId);
        return ResponseEntity.ok().body(new GetQuizResultResponse(response));
    }
}
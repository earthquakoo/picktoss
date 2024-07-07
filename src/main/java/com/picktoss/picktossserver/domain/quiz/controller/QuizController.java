package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.quiz.controller.dto.QuizResponseDto;
import com.picktoss.picktossserver.domain.quiz.controller.mapper.QuizMapper;
import com.picktoss.picktossserver.domain.quiz.controller.request.*;
import com.picktoss.picktossserver.domain.quiz.controller.response.*;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.facade.QuizFacade;
import com.picktoss.picktossserver.global.enums.QuizType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "7. Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class QuizController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizFacade quizFacade;

    @Operation(summary = "Get quiz set")
    @GetMapping("/quiz-sets/{quiz_set_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetResponse> getQuizSet(@PathVariable("quiz_set_id") String quizSetId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizSetResponse response = quizFacade.findQuizSet(quizSetId, memberId);
        return ResponseEntity.ok().body(response);
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

    @Operation(summary = "Get example quiz set")
    @GetMapping("/example-quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetExampleQuizSetResponse> getExampleQuizSet() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetExampleQuizSetResponse response = quizFacade.findExampleQuizSet();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Create quiz")
    @PostMapping("/quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CreateQuizzesResponse> createQuizzes(@Valid @RequestBody CreateQuizzesRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String quizSetId = quizFacade.createQuizzes(request.getDocuments(), request.getPoint(), request.getQuizType(), memberId);
        return ResponseEntity.ok().body(new CreateQuizzesResponse(quizSetId));
    }

    @Operation(summary = "Get all generated quizzes by document")
    @GetMapping("/documents/{document_id}/{quiz_type}/quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> getGeneratedQuizzes(
            @PathVariable("document_id") Long documentId,
            @PathVariable("quiz_type") QuizType quizType
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizFacade.findAllGeneratedQuizzes(documentId, quizType, memberId);
        QuizResponseDto quizResponseDto = QuizMapper.quizzesToQuizResponseDto(quizzes);
        return ResponseEntity.ok().body(quizResponseDto);
    }

    @Operation(summary = "Get bookmarked quiz")
    @GetMapping("/quiz/bookmark")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> getBookmarkedQuiz() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizFacade.findBookmarkQuiz();
        QuizResponseDto quizResponseDto = QuizMapper.quizzesToQuizResponseDto(quizzes);
        return ResponseEntity.ok().body(quizResponseDto);
    }

    @Operation(summary = "Update bookmarked quiz")
    @PatchMapping("/quiz/{quiz_id}/bookmark")
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
    public ResponseEntity<UpdateQuizResultResponse> updateQuizResult(@Valid @RequestBody GetQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        Integer reward = quizFacade.updateQuizResult(request.getQuizzes(), request.getQuizSetId(), memberId);
        return ResponseEntity.ok().body(new UpdateQuizResultResponse(reward));
    }

    @Operation(summary = "Get quiz answer rate analysis by week")
    @GetMapping("/categories/{category_id}/quiz-answer-rate-week")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizAnswerRateAnalysisResponse> getQuizAnswerRateAnalysisByWeek(
            @PathVariable("category_id") Long categoryId,
            @RequestParam(required = false, defaultValue = "1", value = "week") String week
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
        Integer weeks = Integer.valueOf(week);

        GetQuizAnswerRateAnalysisResponse response = quizFacade.findQuizAnswerRateAnalysisByWeek(memberId, categoryId, weeks);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Get quiz answer rate analysis by month")
    @GetMapping("/categories/{category_id}/quiz-answer-rate-month/{year}/{month}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizAnswerRateAnalysisResponse> getQuizAnswerRateAnalysisByMonth(
            @PathVariable("category_id") Long categoryId,
            @PathVariable("year") int year,
            @PathVariable("month") int month
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizAnswerRateAnalysisResponse response = quizFacade.findQuizAnswerRateAnalysisByMonth(memberId, categoryId, year, month);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Get number of quizzes in the document")
    @PostMapping("/documents/quiz-count")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizCountByDocumentResponse> getQuizCountByDocument(
            @Valid @RequestBody GetQuizCountByDocumentRequest request
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizCountByDocumentResponse response = quizFacade.findQuizCountByDocument(request.getDocumentIds(), memberId, request.getType());
        return ResponseEntity.ok().body(response);
    }


    @Operation(summary = "Delete incorrect quiz")
    @DeleteMapping("/incorrect-quiz/{document_id}/{quiz_set_id}/{quiz_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIncorrectQuiz(
            @PathVariable("document_id") Long documentId,
            @PathVariable("quiz_set_id") String quizSetId,
            @PathVariable("quiz_id") Long quizId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizFacade.deleteIncorrectQuiz(quizId, quizSetId, documentId, memberId);
    }

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Tag(name = "Client test 전용 API")
    @Operation(summary = "오늘의 퀴즈 생성 API(테스트 혹은 예외처리를 위한 API로서 실제 사용 X)")
    @PostMapping("/test/create-today-quiz")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CreateQuizzesResponse> createTodayQuizForTest() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String quizSetId = quizFacade.createTodayQuizForTest(memberId);
        return ResponseEntity.ok().body(new CreateQuizzesResponse(quizSetId));
    }
}

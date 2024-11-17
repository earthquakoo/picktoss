package com.picktoss.picktossserver.domain.quiz.controller;

import com.lowagie.text.DocumentException;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.pdfgenerator.PdfGenerator;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.quiz.controller.dto.QuizResponseDto;
import com.picktoss.picktossserver.domain.quiz.controller.mapper.QuizMapper;
import com.picktoss.picktossserver.domain.quiz.controller.request.CreateQuizzesByDocumentRequest;
import com.picktoss.picktossserver.domain.quiz.controller.request.DeleteInvalidQuizRequest;
import com.picktoss.picktossserver.domain.quiz.controller.request.GetQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.response.*;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.facade.QuizFacade;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PdfGenerator pdfGenerator;
    private final QuizFacade quizFacade;

    @Operation(summary = "quizSet_id로 퀴즈 가져오기")
    @GetMapping("/quiz-sets/{quiz_set_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetResponse> getQuizSet(@PathVariable("quiz_set_id") String quizSetId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizSetResponse response = quizFacade.findQuizSet(quizSetId, memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "오늘의 퀴즈 세트 정보 가져오기")
    @GetMapping("/quiz-sets/today")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetTodayResponse> getQuizSetToday() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizSetTodayResponse quizSetToday = quizFacade.findQuizSetToday(memberId);

        return ResponseEntity.ok().body(quizSetToday);
    }

    @Operation(summary = "생성된 모든 퀴즈 가져오기(랜덤 퀴즈)")
    @GetMapping("/quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> getAllQuizzesByMemberId() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizFacade.findAllByMemberId(memberId);
        QuizResponseDto quizResponseDto = QuizMapper.quizzesToQuizResponseDto(quizzes);
        return ResponseEntity.ok().body(quizResponseDto);
    }

    @Operation(summary = "document_id에 해당하는 모든 퀴즈 가져오기")
    @GetMapping("/documents/{document_id}/quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> getGeneratedQuizzes(
            @PathVariable("document_id") Long documentId,
            @RequestParam(required = false, value = "quiz-type") QuizType quizType
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizFacade.findAllGeneratedQuizzesByDocumentId(documentId, quizType, memberId);
        QuizResponseDto quizResponseDto = QuizMapper.quizzesToQuizResponseDto(quizzes);
        return ResponseEntity.ok().body(quizResponseDto);
    }

    @Operation(summary = "document_id로 복습 pick 가져오기")
    @GetMapping("/documents/{document_id}/review-pick")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetDocumentsNeedingReviewPickResponse> getDocumentsNeedingReviewPick(
            @PathVariable(name = "document_id") Long documentId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetDocumentsNeedingReviewPickResponse response = quizFacade.findDocumentsNeedingReviewPick(memberId, documentId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 결과 업데이트")
    @PatchMapping("/quiz/result")
    @ApiErrorCodeExample(QUIZ_SET_NOT_FOUND_ERROR)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UpdateQuizResultResponse> updateQuizResult(@Valid @RequestBody GetQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        UpdateQuizResultResponse response = quizFacade.updateQuizResult(request.getQuizzes(), request.getQuizSetId(), memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 분석")
    @GetMapping("/quiz-analysis")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizAnswerRateAnalysisResponse> getQuizAnswerRateAnalysis(
            @RequestParam(required = false, value = "directory-id") Long directoryId,
            @RequestParam(required = false, value = "week") LocalDate startWeekDate,
            @RequestParam(required = false, value = "month") LocalDate startMonthDate
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizAnswerRateAnalysisResponse response = quizFacade.findQuizAnswerRateAnalysis(memberId, directoryId, startWeekDate, startMonthDate);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 삭제")
    @DeleteMapping("/quizzes/{quiz_id}/delete-quiz")
    @ApiErrorCodeExample(QUIZ_NOT_FOUND_ERROR)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuiz(@PathVariable("quiz_id") Long quizId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizFacade.deleteQuiz(quizId, memberId);
    }

    @Operation(summary = "잘못된 퀴즈 삭제")
    @DeleteMapping("/quizzes/{quiz_id}/delete-invalid-quiz")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, QUIZ_NOT_FOUND_ERROR})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvalidQuiz(
            @PathVariable("quiz_id") Long quizId,
            @Valid @RequestBody DeleteInvalidQuizRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizFacade.deleteInvalidQuiz(quizId, memberId, request.getErrorContent());
    }

    @Operation(summary = "사용자가 생성한 문서에서 직접 퀴즈 생성(랜덤, OX, 객관식)")
    @PostMapping("/quizzes/documents/{document_id}/create-quizzes")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, QUIZ_COUNT_EXCEEDED})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CreateQuizzesResponse> createQuizzesByDocument(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody CreateQuizzesByDocumentRequest request
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String quizSetId = quizFacade.createQuizzesByDocument(documentId, memberId, request.getQuizType(), request.getQuizCount());
        return ResponseEntity.ok().body(new CreateQuizzesResponse(quizSetId));
    }

    @Operation(summary = "전체 퀴즈 기록")
    @GetMapping("/quizzes/quiz-records")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizRecordResponse> getAllQuizzesAndCollectionRecords() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizRecordResponse response = quizFacade.findAllQuizAndCollectionRecords(memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 세트에 대한 상세 기록")
    @GetMapping("/quizzes/{quiz_set_id}/quiz-record")
    @ApiErrorCodeExamples({QUIZ_SET_NOT_FOUND_ERROR, UNRESOLVED_QUIZ_SET})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleQuizSetRecordResponse> getSingleQuizSetRecord(
            @PathVariable("quiz_set_id") String quizSetId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleQuizSetRecordResponse response = quizFacade.findQuizSetRecordByMemberIdAndQuizSetId(memberId, quizSetId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "오늘의 퀴즈 현황")
    @GetMapping("/today-quiz-info")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetCurrentTodayQuizInfo> getCurrentTodayQuizInfo() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetCurrentTodayQuizInfo response = quizFacade.findCurrentTodayQuizInfo(memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 다운로드")
    @GetMapping("/documents/{document_id}/download-quiz")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> downloadQuizzes(@PathVariable("document_id") Long documentId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        try {
            List<Quiz> quizzes = quizFacade.findAllByDocumentIdAndMemberId(documentId, memberId);
            byte[] pdfBytes = pdfGenerator.generateQuizPdf(quizzes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "quizzes.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (DocumentException | IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Test API
     */

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

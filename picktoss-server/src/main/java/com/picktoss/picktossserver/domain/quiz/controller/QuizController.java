package com.picktoss.picktossserver.domain.quiz.controller;

import com.lowagie.text.DocumentException;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.pdfgenerator.PdfGenerator;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.quiz.dto.mapper.QuizResponseDto;
import com.picktoss.picktossserver.domain.quiz.dto.mapper.QuizMapper;
import com.picktoss.picktossserver.domain.quiz.dto.request.CreateQuizzesByDocumentRequest;
import com.picktoss.picktossserver.domain.quiz.dto.request.DeleteInvalidQuizRequest;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateRandomQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.dto.response.*;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.service.*;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final QuizAnalysisService quizAnalysisService;
    private final QuizCreateService quizCreateService;
    private final QuizDeleteService quizDeleteService;
    private final QuizRecordService quizRecordService;
    private final QuizResultUpdateService quizResultUpdateService;
    private final QuizSearchService quizSearchService;
    private final TodayQuizSetService todayQuizSetService;
    private final QuizDownloadService quizDownloadService;

    /**
     * GET
     */

    /*
     * QuizSearchService
     */

    @Operation(summary = "quiz_set_id와 quiz-set-type으로 퀴즈 가져오기")
    @GetMapping("/quiz-sets/{quiz_set_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetResponse> getQuizSets(
            @RequestParam(value = "quiz-set-type") QuizSetType quizSetType,
            @PathVariable("quiz_set_id") String quizSetId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizSetResponse response = quizSearchService.findQuizSetByQuizSetIdAndQuizSetType(quizSetId, quizSetType, memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "디렉토리에 생성된 모든 퀴즈 랜덤하게 가져오기(랜덤 퀴즈)")
    @GetMapping("/directories/{directory_id}/quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllQuizzesByDirectoryIdResponse> getAllQuizzesByMemberId(
            @PathVariable("directory_id") Long directoryId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetAllQuizzesByDirectoryIdResponse response = quizSearchService.findAllByMemberIdAndDirectoryId(memberId, directoryId);
        return ResponseEntity.ok().body(response);
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

        List<Quiz> quizzes = quizSearchService.findAllGeneratedQuizzesByDocumentId(documentId, quizType, memberId);
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

        GetDocumentsNeedingReviewPickResponse response = quizSearchService.findDocumentsNeedingReviewPick(memberId, documentId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "오답 터뜨리기 퀴즈 가져오기")
    @GetMapping("/incorrect-quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> getIncorrectQuizzes() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizSearchService.findIncorrectQuizzesByMemberIdAndIsReviewNeedTrue(memberId);
        QuizResponseDto quizResponseDto = QuizMapper.quizzesToQuizResponseDto(quizzes);
        return ResponseEntity.ok().body(quizResponseDto);
    }

    /*
     * TodayQuizSetService
     */

    @Operation(summary = "오늘의 퀴즈 세트 정보 가져오기")
    @GetMapping("/quiz-sets/today")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetTodayResponse> getQuizSetToday() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizSetTodayResponse quizSetToday = todayQuizSetService.findQuizSetToday(memberId);
        return ResponseEntity.ok().body(quizSetToday);
    }

    @Operation(summary = "오늘의 퀴즈 현황")
    @GetMapping("/today-quiz-info")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetCurrentTodayQuizInfo> getCurrentTodayQuizInfo() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetCurrentTodayQuizInfo response = todayQuizSetService.findCurrentTodayQuizInfo(memberId);
        return ResponseEntity.ok().body(response);
    }

    /*
     * QuizAnalysisService
     */

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

    /*
     * QuizRecordService
     */

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

    /*
     * QuizDownloadService
     */

    @Operation(summary = "퀴즈 다운로드")
    @GetMapping("/documents/{document_id}/download-quiz")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> downloadQuizzes(@PathVariable("document_id") Long documentId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        try {
            List<Quiz> quizzes = quizDownloadService.findAllByDocumentIdAndMemberId(documentId, memberId);
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
     * PATCH
     */

    /*
     * QuizResultUpdateService
     */

    @Operation(summary = "퀴즈 결과 업데이트")
    @PatchMapping("/quiz/result")
    @ApiErrorCodeExample(QUIZ_SET_NOT_FOUND_ERROR)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UpdateQuizResultResponse> updateQuizResult(@Valid @RequestBody UpdateQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        UpdateQuizResultResponse response = quizResultUpdateService.updateQuizResult(request.getQuizzes(), request.getQuizSetId(), request.getQuizSetType(), memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "랜덤 퀴즈 결과 업데이트")
    @PatchMapping("/random-quiz/result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRandomQuizResult(@Valid @RequestBody UpdateRandomQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizResultUpdateService.updateRandomQuizResult(request.getQuizzes(), memberId);
    }

    @Operation(summary = "오답 터뜨리기 결과 업데이트")
    @PatchMapping("/wrong-quiz/result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateWrongQuizResult(@Valid @RequestBody UpdateRandomQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizResultUpdateService.updateWrongQuizResult(request.getQuizzes(), memberId);
    }

    /**
     * POST
     */

    /*
     * QuizCreateService
     */

    @Operation(summary = "사용자가 생성한 문서에서 직접 퀴즈 세트 생성(랜덤, OX, 객관식)")
    @PostMapping("/quizzes/documents/{document_id}/custom-quiz-set")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, QUIZ_COUNT_EXCEEDED, QUIZ_TYPE_NOT_IN_DOCUMENT})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateQuizzesResponse> createMemberGeneratedQuizSet(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody CreateQuizzesByDocumentRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        CreateQuizzesResponse response = quizCreateService.createMemberGeneratedQuizSet(documentId, memberId, request.getQuizType(), request.getQuizCount());
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "퀴즈 생성 후 퀴즈 오류 확인을 위한 퀴즈세트 생성(퀴즈 시작하기 후 모든 퀴즈 생성이 완료되면 요청)")
    @PostMapping("/quizzes/documents/{document_id}/check-quiz-set")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateQuizzesResponse> createErrorCheckQuizSet(
            @PathVariable("document_id") Long documentId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        CreateQuizzesResponse response = quizCreateService.createErrorCheckQuizSet(documentId, memberId);
        return ResponseEntity.ok().body(response);
    }

    /**
     * DELETE
     */

    /*
     * QuizDeleteService
     */

    @Operation(summary = "퀴즈 삭제")
    @DeleteMapping("/quizzes/{quiz_id}/delete-quiz")
    @ApiErrorCodeExample(QUIZ_NOT_FOUND_ERROR)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuiz(@PathVariable("quiz_id") Long quizId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizDeleteService.deleteQuiz(quizId, memberId);
    }

    @Operation(summary = "오류가 발생한 퀴즈 삭제")
    @DeleteMapping("/quizzes/{quiz_id}/invalid")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, QUIZ_NOT_FOUND_ERROR})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvalidQuiz(
            @PathVariable("quiz_id") Long quizId,
            @Valid @RequestBody DeleteInvalidQuizRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizDeleteService.deleteInvalidQuiz(quizId, memberId, request.getQuizErrorType());
    }

    /**
     * Test API
     */

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
//    @Tag(name = "Client test 전용 API")
//    @Operation(summary = "오늘의 퀴즈 생성 API(테스트 혹은 예외처리를 위한 API로서 실제 사용 X)")
//    @PostMapping("/test/create-today-quiz")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<CreateQuizzesResponse> createTodayQuizForTest() {
//        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
//        Long memberId = jwtUserInfo.getMemberId();
//
//        CreateQuizzesResponse response = quizFacade.createTodayQuizForTest(memberId);
//        return ResponseEntity.ok().body(response);
//    }
}

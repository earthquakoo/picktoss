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
import com.picktoss.picktossserver.domain.quiz.controller.request.UpdateQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.request.UpdateRandomQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.response.*;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.facade.QuizFacade;
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

    /**
     * GET
     */

    @Operation(summary = "quiz_set_id와 collection_id로 컬렉션 퀴즈 가져오기")
    @GetMapping("/collections/{collection_id}/quiz-sets/{quiz_set_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetByCollectionResponse> getQuizSetByCollection(
            @PathVariable("collection_id") Long collectionId,
            @PathVariable("quiz_set_id") String quizSetId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizSetByCollectionResponse response = quizFacade.findQuizSetByCollection(quizSetId, collectionId, memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "quiz_set_id로 문서 퀴즈 가져오기")
    @GetMapping("/documents/quiz-sets/{quiz_set_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuizSetByDocumentResponse> getQuizSetByDocument(
            @PathVariable("quiz_set_id") String quizSetId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuizSetByDocumentResponse response = quizFacade.findQuizSetByDocument(quizSetId, memberId);
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

    @Operation(summary = "디렉토리에 생성된 모든 퀴즈 랜덤하게 가져오기(랜덤 퀴즈)")
    @GetMapping("/directories/{directory_id}/quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> getAllQuizzesByMemberId(
            @PathVariable("directory_id") Long directoryId
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizFacade.findAllByMemberIdAndDirectoryId(memberId, directoryId);
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
    @GetMapping("/quizzes/{quiz_set_id}/{quiz_set_type}/quiz-record")
    @ApiErrorCodeExamples({QUIZ_SET_NOT_FOUND_ERROR, UNRESOLVED_QUIZ_SET, QUIZ_SET_TYPE_ERROR})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleQuizSetRecordResponse> getSingleQuizSetRecord(
            @PathVariable("quiz_set_id") String quizSetId,
            @PathVariable("quiz_set_type") QuizSetType quizSetType
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleQuizSetRecordResponse response = quizFacade.findQuizSetRecordByMemberIdAndQuizSetId(memberId, quizSetId, quizSetType);
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

    @Operation(summary = "오답 터뜨리기 퀴즈 가져오기")
    @GetMapping("/incorrect-quizzes")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<QuizResponseDto> getIncorrectQuizzes() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<Quiz> quizzes = quizFacade.findIncorrectQuizzesByMemberIdAndIsReviewNeedTrue(memberId);
        QuizResponseDto quizResponseDto = QuizMapper.quizzesToQuizResponseDto(quizzes);
        return ResponseEntity.ok().body(quizResponseDto);
    }

    /**
     * PATCH
     */

    @Operation(summary = "퀴즈 결과 업데이트")
    @PatchMapping("/quiz/result")
    @ApiErrorCodeExample(QUIZ_SET_NOT_FOUND_ERROR)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UpdateQuizResultResponse> updateQuizResult(@Valid @RequestBody UpdateQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        UpdateQuizResultResponse response = quizFacade.updateQuizResult(request.getQuizzes(), request.getQuizSetId(), memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "랜덤 퀴즈 결과 업데이트")
    @PatchMapping("/random-quiz/result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRandomQuizResult(@Valid @RequestBody UpdateRandomQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizFacade.updateRandomQuizResult(request.getQuizzes(), memberId);
    }

    @Operation(summary = "오답 터뜨리기 결과 업데이트")
    @PatchMapping("/wrong-quiz/result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateWrongQuizResult(@Valid @RequestBody UpdateRandomQuizResultRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizFacade.updateWrongQuizResult(request.getQuizzes(), memberId);
    }

    /**
     * POST
     */

    @Operation(summary = "사용자가 생성한 문서에서 직접 퀴즈 세트 생성(랜덤, OX, 객관식)")
    @PostMapping("/quizzes/documents/{document_id}/custom-quiz-set")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, QUIZ_COUNT_EXCEEDED})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateQuizzesResponse> createMemberGeneratedQuizSet(
            @PathVariable("document_id") Long documentId,
            @Valid @RequestBody CreateQuizzesByDocumentRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        CreateQuizzesResponse response = quizFacade.createMemberGeneratedQuizSet(documentId, memberId, request.getQuizType(), request.getQuizCount());
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

        CreateQuizzesResponse response = quizFacade.createErrorCheckQuizSet(documentId, memberId);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "컬렉션 퀴즈 시작하기")
    @PostMapping("/collections/{collection_id}/collection-quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateQuizzesResponse> createCollectionQuizSet(@PathVariable("collection_id") Long collectionId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        CreateQuizzesResponse response = quizFacade.createCollectionQuizSet(collectionId, memberId);
        return ResponseEntity.ok().body(response);
    }

    /**
     * DELETE
     */

    @Operation(summary = "퀴즈 삭제")
    @DeleteMapping("/quizzes/{quiz_id}/delete-quiz")
    @ApiErrorCodeExample(QUIZ_NOT_FOUND_ERROR)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuiz(@PathVariable("quiz_id") Long quizId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        quizFacade.deleteQuiz(quizId, memberId);
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

        quizFacade.deleteInvalidQuiz(quizId, memberId, request.getQuizErrorType());
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

        CreateQuizzesResponse response = quizFacade.createTodayQuizForTest(memberId);
        return ResponseEntity.ok().body(response);
    }
}

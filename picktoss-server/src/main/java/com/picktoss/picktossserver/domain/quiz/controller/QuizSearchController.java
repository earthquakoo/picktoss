package com.picktoss.picktossserver.domain.quiz.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.quiz.dto.mapper.QuizMapper;
import com.picktoss.picktossserver.domain.quiz.dto.mapper.QuizResponseDto;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetAllQuizzesByDirectoryIdResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetDocumentsNeedingReviewPickResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizSetResponse;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.service.QuizSearchService;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quiz")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class QuizSearchController {

    private final JwtTokenProvider jwtTokenProvider;
    private final QuizSearchService quizSearchService;

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
}

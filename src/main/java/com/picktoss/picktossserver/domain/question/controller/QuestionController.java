package com.picktoss.picktossserver.domain.question.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.question.controller.response.GetAllCategoryQuestionsResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetTodayResponse;
import com.picktoss.picktossserver.domain.question.facade.QuestionFacade;
import com.picktoss.picktossserver.domain.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class QuestionController {

    private final QuestionFacade questionFacade;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Get all category questions by document")
    @GetMapping("/categories/{category_id}/documents/questions")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllCategoryQuestionsResponse> getAllCategoryQuestions(@PathVariable("category_id") Long categoryId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetAllCategoryQuestionsResponse.DocumentDto> allCategoryQuestions = questionFacade.findAllCategoryQuestions(categoryId, memberId);
        return ResponseEntity.ok().body(new GetAllCategoryQuestionsResponse(allCategoryQuestions));
    }

    @Operation(summary = "Get question set today")
    @GetMapping("/question-sets/today")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuestionSetTodayResponse> getQuestionSetToday() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetQuestionSetTodayResponse responseBody = questionFacade.findQuestionSetToday(memberId);
        return ResponseEntity.ok().body(responseBody);
    }

    @Operation(summary = "Get question set")
    @GetMapping("/question-sets/{question_set_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetQuestionSetResponse> getQuestionSet(@PathVariable("question_set_id") String questionSetId) {
        List<GetQuestionSetResponse.QuestionDto> questionSet = questionFacade.findQuestionSet(questionSetId);
        return ResponseEntity.ok().body(new GetQuestionSetResponse(questionSet));
    }
}

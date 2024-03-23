package com.picktoss.picktossserver.domain.question.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.question.controller.response.GetAllCategoryQuestionsResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetTodayResponse;
import com.picktoss.picktossserver.domain.question.facade.QuestionFacade;
import com.picktoss.picktossserver.domain.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class QuestionController {

    private final QuestionFacade questionFacade;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/categories/{category_id}/documents/questions")
    public ResponseEntity<GetAllCategoryQuestionsResponse> getAllCategoryQuestions(@PathVariable("category_id") Long categoryId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        String memberId = jwtUserInfo.getMemberId();

        List<GetAllCategoryQuestionsResponse.DocumentDto> allCategoryQuestions = questionFacade.findAllCategoryQuestions(categoryId);
        return ResponseEntity.ok().body(new GetAllCategoryQuestionsResponse(allCategoryQuestions));
    }

    @GetMapping("/question-sets/today")
    public ResponseEntity<GetQuestionSetTodayResponse> getQuestionSetToday() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        String memberId = jwtUserInfo.getMemberId();

        GetQuestionSetTodayResponse responseBody = questionFacade.findQuestionSetToday(memberId);
        return ResponseEntity.ok().body(responseBody);
    }

    @GetMapping("/question-sets/{question_set_id}")
    public ResponseEntity<GetQuestionSetResponse> getQuestionSet(@PathVariable("question_set_id") String questionSetId) {
        List<GetQuestionSetResponse.QuestionDto> questionSet = questionFacade.findQuestionSet(questionSetId);
        return ResponseEntity.ok().body(new GetQuestionSetResponse(questionSet));
    }
}

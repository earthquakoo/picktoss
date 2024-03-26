package com.picktoss.picktossserver.domain.question.facade;


import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.question.controller.response.GetAllCategoryQuestionsResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetTodayResponse;
import com.picktoss.picktossserver.domain.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionFacade {

    private final QuestionService questionService;
    private final DocumentService documentService;

    public List<GetAllCategoryQuestionsResponse.DocumentDto> findAllCategoryQuestions(Long categoryId, Long memberId) {
        List<Document> documents = documentService.findAllByCategoryIdAndMemberId(categoryId, memberId);
        return questionService.findAllCategoryQuestions(documents);
    }

    public List<GetQuestionSetResponse.QuestionDto> findQuestionSet(String questionSetId) {
        return questionService.findQuestionSet(questionSetId);
    }

    public GetQuestionSetTodayResponse findQuestionSetToday(Long memberId) {
        List<Document> documents = documentService.findAllByMemberId(memberId);
        return questionService.findQuestionSetToday(memberId, documents);
    }
}

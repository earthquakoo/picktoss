package com.picktoss.picktossserver.domain.quiz.facade;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetTodayResponse;
import com.picktoss.picktossserver.domain.quiz.controller.response.GetQuizSetResponse;
import com.picktoss.picktossserver.domain.quiz.controller.response.GetQuizSetTodayResponse;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizFacade {

    private final DocumentService documentService;
    private final QuizService quizService;

    public List<GetQuizSetResponse.GetQuizSetQuizDto> findQuestionSet(String quizSetId) {
        return quizService.findQuestionSet(quizSetId);
    }

    public GetQuizSetTodayResponse findQuizSetToday(Long memberId) {
        List<Document> documents = documentService.findAllByMemberId(memberId);
        return quizService.findQuestionSetToday(memberId, documents);
    }
}

package com.picktoss.picktossserver.domain.quiz.facade;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetTodayResponse;
import com.picktoss.picktossserver.domain.quiz.controller.response.GetBookmarkQuizResponse;
import com.picktoss.picktossserver.domain.quiz.controller.response.GetQuizSetResponse;
import com.picktoss.picktossserver.domain.quiz.controller.response.GetQuizSetTodayResponse;
import com.picktoss.picktossserver.domain.quiz.controller.response.GetSingleQuizResponse;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
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

    public GetSingleQuizResponse findQuiz(Long quizId) {
        return quizService.findQuiz(quizId);
    }

    public List<GetQuizSetResponse.GetQuizSetQuizDto> findQuizSet(String quizSetId) {
        return quizService.findQuizSet(quizSetId);
    }

    public GetQuizSetTodayResponse findQuizSetToday(Long memberId) {
        List<Document> documents = documentService.findAllByMemberId(memberId);
        return quizService.findQuestionSetToday(memberId, documents);
    }

    public List<GetBookmarkQuizResponse.GetBookmarkQuizDto> findBookmarkQuiz() {
        return quizService.findBookmarkQuiz();
    }

    @Transactional
    public void updateBookmarkQuiz(Long quizId, boolean bookmark) {
        quizService.updateBookmarkQuiz(quizId, bookmark);
    }
}

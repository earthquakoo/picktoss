package com.picktoss.picktossserver.domain.quiz.facade;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.event.entity.Event;
import com.picktoss.picktossserver.domain.event.service.EventService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.quiz.controller.dto.QuizResponseDto;
import com.picktoss.picktossserver.domain.quiz.controller.request.GetQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.response.*;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.global.enums.QuizType;
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
    private final MemberService memberService;
    private final EventService eventService;

    public List<Quiz> findQuizSet(String quizSetId, Long memberId) {
        return quizService.findQuizSet(quizSetId, memberId);
    }

    public GetQuizSetTodayResponse findQuizSetToday(Long memberId) {
        List<Document> documents = documentService.findAllByMemberId(memberId);
        return quizService.findQuestionSetToday(memberId, documents);
    }

    public List<Quiz> createQuizzes(List<Long> documents, int point, QuizType quizType, Long memberId) {
        Event event = eventService.findEventByMemberId(memberId);
        return quizService.createQuizzes(documents, point, quizType, event);
    }

    public List<Quiz> findAllGeneratedQuizzes(Long memberId) {
        return quizService.findAllGeneratedQuizzes(memberId);
    }

    public List<Quiz> findBookmarkQuiz() {
        return quizService.findBookmarkQuiz();
    }

    @Transactional
    public void updateBookmarkQuiz(Long quizId, boolean bookmark) {
        quizService.updateBookmarkQuiz(quizId, bookmark);
    }

    @Transactional
    public List<GetQuizResultResponse.GetQuizResultCategoryDto> updateQuizResult(List<GetQuizResultRequest.GetQuizResultQuizDto> resultQuizDtos, String quizSetId, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        return quizService.updateQuizResult(resultQuizDtos, quizSetId, member);
    }
}

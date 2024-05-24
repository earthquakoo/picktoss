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

import java.util.HashMap;
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

    @Transactional
    public List<Quiz> createQuizzes(List<Long> documents, int point, QuizType quizType, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        List<Quiz> quizzes = quizService.createQuizzes(documents, point, quizType, member);
        Event event = eventService.findEventByMemberId(memberId);
        event.usePoint(point);
        return quizzes;
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
    public void updateQuizResultList(List<GetQuizResultRequest.GetQuizResultQuizDto> quizzes, String quizSetId, Long memberId) {
        quizService.updateQuizResult(quizzes, quizSetId, memberId);
        Member member = memberService.findMemberById(memberId);
        member.updateContinuousQuizDatesCount(true);
    }

    public GetQuizAnalysisResponse findQuizAnalysisByCategory(Long memberId, Long categoryId) {
        return quizService.findQuizAnalysisByCategory(memberId, categoryId);
    }

    public List<GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto> findQuizAnswerRateAnalysisByCategory(Long memberId, Long categoryId, int weeks) {
        return quizService.findQuizAnswerRateAnalysisByCategory(memberId, categoryId, weeks);
    }
}

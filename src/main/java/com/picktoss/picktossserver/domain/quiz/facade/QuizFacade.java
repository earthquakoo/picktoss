package com.picktoss.picktossserver.domain.quiz.facade;

import com.picktoss.picktossserver.core.exception.CustomException;
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
import com.picktoss.picktossserver.global.enums.QuizSetResponseType;
import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.POINT_NOT_ENOUGH;
import static com.picktoss.picktossserver.domain.event.constant.EventConstant.FIVE_DAYS_CONTINUOUS_POINT;
import static com.picktoss.picktossserver.domain.event.constant.EventConstant.ONE_DAYS_POINT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizFacade {

    private final DocumentService documentService;
    private final QuizService quizService;
    private final MemberService memberService;
    private final EventService eventService;

    public GetQuizSetResponse findQuizSet(String quizSetId, Long memberId) {
        return quizService.findQuizSet(quizSetId, memberId);
    }

    public GetQuizSetTodayResponse findQuizSetToday(Long memberId) {
        List<Document> documents = documentService.findAllByMemberId(memberId);
        if (documents.isEmpty()) {
            return GetQuizSetTodayResponse.builder()
                    .type(QuizSetResponseType.NOT_READY)
                    .build();
        }
        return quizService.findQuestionSetToday(memberId);
    }

    @Transactional
    public String createQuizzes(List<Long> documents, int point, QuizType quizType, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Event event = eventService.findEventByMemberId(memberId);

        if (point > event.getPoint()) {
            throw new CustomException(POINT_NOT_ENOUGH);
        }

        String quizSetId = quizService.createQuizzes(documents, point, quizType, member);

        event.usePointByGenerateQuiz(point);
        return quizSetId;
    }

    public List<Quiz> findAllGeneratedQuizzes(Long documentId, QuizType quizType, Long memberId) {
        return quizService.findAllGeneratedQuizzes(documentId, quizType, memberId);
    }

    public List<Quiz> findBookmarkQuiz() {
        return quizService.findBookmarkQuiz();
    }

    @Transactional
    public void updateBookmarkQuiz(Long quizId, boolean bookmark) {
        quizService.updateBookmarkQuiz(quizId, bookmark);
    }

    @Transactional
    public Integer updateQuizResult(List<GetQuizResultRequest.GetQuizResultQuizDto> quizzes, String quizSetId, Long memberId) {
        boolean isTodayQuizSet = quizService.updateQuizResult(quizzes, quizSetId, memberId);
        if (isTodayQuizSet) {
            Event event = eventService.findEventByMemberId(memberId);
            quizService.checkContinuousQuizDatesCount(memberId, event);
            event.addContinuousSolvedQuizDateCount();

            if (event.getContinuousSolvedQuizDateCount() >= event.getMaxContinuousSolvedQuizDateCount()) {
                event.updateMaxContinuousSolvedQuizDateCount(event.getContinuousSolvedQuizDateCount());
            }

            if ((event.getContinuousSolvedQuizDateCount() % 5) == 0) {
                event.addPointBySolvingTodayQuizFiveContinuousDays();
                return FIVE_DAYS_CONTINUOUS_POINT;
            } else {
                event.addPointBySolvingTodayQuizOneContinuousDays();
                return ONE_DAYS_POINT;
            }
        }
        return null;
    }

    public GetQuizAnswerRateAnalysisResponse findQuizAnswerRateAnalysisByWeek(Long memberId, Long categoryId, int weeks) {
        return quizService.findQuizAnswerRateAnalysisByWeek(memberId, categoryId, weeks);
    }

    public GetQuizAnswerRateAnalysisResponse findQuizAnswerRateAnalysisByMonth(Long memberId, Long categoryId, int year, int month) {
        return quizService.findQuizAnswerRateAnalysisByMonth(memberId, categoryId, year, month);
    }

    public GetQuizCountByDocumentResponse findQuizCountByDocument(List<Long> documentIds, Long memberId, QuizType type) {
        return quizService.findQuizCountByDocument(documentIds, memberId, type);
    }

    @Transactional
    public void deleteIncorrectQuiz(Long quizId, String quizSetId, Long documentId, Long memberId) {
        quizService.deleteIncorrectQuiz(quizId, documentId);
        boolean isTodayQuizSet = quizService.checkTodayQuizSet(quizSetId, memberId);
        if (!isTodayQuizSet) {
            Event event = eventService.findEventByMemberId(memberId);
            event.addOnePointWithIncorrectlyGeneratedQuiz();
        }
    }

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Transactional
    public String createTodayQuizForTest(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        return quizService.createTodayQuizForTest(member);
    }
}

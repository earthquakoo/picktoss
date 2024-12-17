package com.picktoss.picktossserver.domain.quiz.facade;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.service.CollectionService;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.quiz.constant.QuizConstant;
import com.picktoss.picktossserver.domain.quiz.controller.request.UpdateQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.request.UpdateRandomQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.response.*;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.service.StarService;
import com.picktoss.picktossserver.global.enums.quiz.QuizErrorType;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetResponseType;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizFacade {

    private final DocumentService documentService;
    private final QuizService quizService;
    private final MemberService memberService;
    private final StarService starService;
    private final CollectionService collectionService;

    public GetQuizSetResponse findQuizSetByQuizSetIdAndQuizSetType(String quizSetId, QuizSetType quizSetType, Long memberId) {
        return quizService.findQuizSetByQuizSetIdAndQuizSetType(quizSetId, quizSetType, memberId);
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

    public GetAllQuizzesByDirectoryIdResponse findAllByMemberIdAndDirectoryId(Long memberId, Long directoryId) {
        return quizService.findAllByMemberIdAndDirectoryId(memberId, directoryId);
    }

    public List<Quiz> findAllGeneratedQuizzesByDocumentId(Long documentId, QuizType quizType, Long memberId) {
        return quizService.findAllGeneratedQuizzesByDocumentId(documentId, quizType, memberId);
    }

    public GetDocumentsNeedingReviewPickResponse findDocumentsNeedingReviewPick(Long memberId, Long documentId) {
        return quizService.findDocumentsNeedingReviewPick(memberId, documentId);
    }

    @Transactional
    public UpdateQuizResultResponse updateQuizResult(List<UpdateQuizResultRequest.UpdateQuizResultQuizDto> quizzes, String quizSetId, Long memberId) {
        QuizSetType quizSetType = quizService.updateQuizResult(quizzes, quizSetId, memberId);
        if (quizSetType == QuizSetType.TODAY_QUIZ_SET) {
            List<QuizSet> quizSets = quizService.findAllByMemberIdAndSolvedTrueAndQuizSetTypeOrderByCreatedAtDesc(memberId);
            int currentConsecutiveTodayQuizDate = quizService.checkCurrentConsecutiveTodayQuiz(quizSets);
            if (currentConsecutiveTodayQuizDate % 5 == 0) {
                return new UpdateQuizResultResponse(QuizConstant.FIVE_DAYS_CONSECUTIVE_REWARD, currentConsecutiveTodayQuizDate);
            }
            return new UpdateQuizResultResponse(QuizConstant.FIVE_DAYS_CONSECUTIVE_REWARD, currentConsecutiveTodayQuizDate);
        }
        return null;
    }

    @Transactional
    public CreateQuizzesResponse createMemberGeneratedQuizSet(Long documentId, Long memberId, String quizType, Integer quizCount) {
        Member member = memberService.findMemberById(memberId);
        return quizService.createMemberGeneratedQuizSet(documentId, member, quizType, quizCount);
    }

    @Transactional
    public CreateQuizzesResponse createErrorCheckQuizSet(Long documentId, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        return quizService.createErrorCheckQuizSet(documentId, member);
    }

    @Transactional
    public CreateQuizzesResponse createCollectionQuizSet(Long collectionId, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Collection collection = collectionService.findCollectionByCollectionId(collectionId);
        return quizService.createCollectionQuizSet(member, collection);
    }

    public GetSingleQuizRecordByDateResponse findAllQuizSetRecordByDate(Long memberId, LocalDate solvedDate) {
        return quizService.findAllQuizSetRecordByDate(memberId, solvedDate);
    }

    public GetQuizRecordsResponse findAllQuizAndCollectionRecords(Long memberId) {
        return quizService.findAllQuizAndCollectionRecords(memberId);
    }

    public GetSingleQuizSetRecordResponse findQuizSetRecordByMemberIdAndQuizSetId(Long memberId, String quizSetId, QuizSetType quizSetType) {
        return quizService.findQuizSetRecordByMemberIdAndQuizSetId(memberId, quizSetId, quizSetType);
    }

    public GetQuizAnswerRateAnalysisResponse findQuizAnswerRateAnalysis(Long memberId, Long directoryId, LocalDate startWeekDate, LocalDate startMonthDate) {
        return quizService.findQuizAnswerRateAnalysis(memberId, directoryId, startWeekDate, startMonthDate);
    }

    public GetCurrentTodayQuizInfo findCurrentTodayQuizInfo(Long memberId) {
        return quizService.findCurrentTodayQuizInfo(memberId);
    }

    public List<Quiz> findAllByDocumentIdAndMemberId(Long documentId, Long memberId) {
        return quizService.findAllByDocumentIdAndMemberId(documentId, memberId);
    }

    @Transactional
    public void deleteQuiz(Long quizId, Long memberId) {
        quizService.deleteQuiz(quizId, memberId);
    }

    @Transactional
    public void deleteInvalidQuiz(Long quizId, Long memberId, QuizErrorType quizErrorType) {
        Member member = memberService.findMemberById(memberId);
        Star star = member.getStar();
        starService.depositStarByInvalidQuiz(star, quizErrorType.toString());
        quizService.deleteInvalidQuiz(quizId, memberId);
    }

    @Transactional
    public void updateRandomQuizResult(List<UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto> quizDtos, Long memberId) {
        quizService.updateRandomQuizResult(quizDtos, memberId);
    }

    @Transactional
    public void updateWrongQuizResult(List<UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto> quizDtos, Long memberId) {
        quizService.updateWrongQuizResult(quizDtos, memberId);
    }

    public List<Quiz> findIncorrectQuizzesByMemberIdAndIsReviewNeedTrue(Long memberId) {
        return quizService.findIncorrectQuizzesByMemberIdAndIsReviewNeedTrue(memberId);
    }

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Transactional
    public CreateQuizzesResponse createTodayQuizForTest(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        return quizService.createTodayQuizForTest(member);
    }
}

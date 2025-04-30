package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.dto.response.UpdateQuizResultResponse;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.DOCUMENT_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_SET_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizUpdateService {

    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;
    private final DocumentRepository documentRepository;

    @Transactional
    public UpdateQuizResultResponse updateQuizResult(List<UpdateQuizResultRequest.UpdateQuizResultQuizDto> quizDtos, Long quizSetId, Long memberId, Long documentId) {
        QuizSet quizSet = quizSetRepository.findQuizSetByMemberIdAndQuizSetId(memberId, quizSetId)
                .orElseThrow(() -> new CustomException(QUIZ_SET_NOT_FOUND_ERROR));

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new CustomException(DOCUMENT_NOT_FOUND));
        document.updateDocumentTryCountBySolvedQuizSet();

        boolean isOwner = false;
        Member member = document.getDirectory().getMember();

        if (Objects.equals(member.getId(), memberId)) {
            isOwner = true;
        }

        List<QuizSetQuiz> quizSetQuizzes = quizSet.getQuizSetQuizzes();

        Map<Long, UpdateQuizResultRequest.UpdateQuizResultQuizDto> quizDtoMap = new HashMap<>();
        for (UpdateQuizResultRequest.UpdateQuizResultQuizDto quizDto : quizDtos) {
            quizDtoMap.put(quizDto.getId(), quizDto);
        }

        int totalQuizCount = 0;
        int totalElapsedTime = 0;
        int correctAnswerCount = 0;

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            Quiz quiz = quizSetQuiz.getQuiz();
            UpdateQuizResultRequest.UpdateQuizResultQuizDto quizDto = quizDtoMap.get(quiz.getId());

            if (quizDto == null) continue;

            boolean isCorrect = quizDto.isAnswer();
            int elapsedTime = quizDto.getElapsedTime();

            if (isOwner) {
                if (isCorrect) {
                    quiz.addCorrectAnswerCount();
                } else {
                    quiz.updateIsReviewNeededTrueByWrongAnswer();
                }
            } else if (isCorrect) {
                correctAnswerCount += 1;
            }

            quizSetQuiz.updateIsAnswer(isCorrect);
            quizSetQuiz.updateChoseAnswer(quizDto.getChoseAnswer());
            quizSetQuiz.updateElapsedTime(elapsedTime);

            totalQuizCount += 1;
            totalElapsedTime += elapsedTime;
        }
        quizSet.updateSolvedBySolvedQuizSet();

        double correctAnswerRate = (double) correctAnswerCount / (double) totalQuizCount * 100.0;

        return new UpdateQuizResultResponse(totalQuizCount, totalElapsedTime, correctAnswerRate);
    }

    @Transactional
    public void updateQuizInfo(Long quizId, Long memberId, String question, String answer, String explanation, List<String> options) {
        Quiz quiz = quizRepository.findByQuizIdAndMemberId(quizId, memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.QUIZ_NOT_FOUND_ERROR));

        Set<Option> optionSet = quiz.getOptions();
        List<Option> optionList = new ArrayList<>(optionSet);
        for (int i = 0; i < optionSet.size(); i++) {
            optionList.get(i).updateOptionContent(options.get(i));
        }

        Set<Option> newOptionSet = new HashSet<>(optionList);

        quiz.updateQuizInfoByInvalidQuiz(question, answer, explanation, newOptionSet);
    }

    @Transactional
    public void updateIsReviewNeededByWrongAnswerQuizConfirm(Long quizId, Long memberId) {
        Quiz quiz = quizRepository.findByQuizIdAndMemberId(quizId, memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.QUIZ_NOT_FOUND_ERROR));

        quiz.updateIsReviewNeededFalseByWrongAnswerConfirm();
    }
}

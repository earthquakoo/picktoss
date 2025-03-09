package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.global.enums.quiz.QuizErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizDeleteService {

    private final QuizRepository quizRepository;
    private final MemberRepository memberRepository;
    private final DocumentRepository documentRepository;
    private final StarHistoryRepository starHistoryRepository;

    @Transactional
    public Document deleteQuiz(Long quizId, Long memberId) {
        Quiz quiz = quizRepository.findByQuizIdAndMemberId(quizId, memberId)
                .orElseThrow(() -> new CustomException(QUIZ_NOT_FOUND_ERROR));

        Document document = quiz.getDocument();

        quizRepository.delete(quiz);
        return document;
    }

    @Transactional
    public void deleteInvalidQuiz(Long quizId, Long memberId, QuizErrorType quizErrorType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Star star = member.getStar();
        StarHistory starHistory = star.depositStarByInvalidQuiz(star, quizErrorType.toString());
        starHistoryRepository.save(starHistory);

        Quiz quiz = quizRepository.findByQuizIdAndMemberId(quizId, memberId)
                .orElseThrow(() -> new CustomException(QUIZ_NOT_FOUND_ERROR));

        quizRepository.delete(quiz);
    }

    @Transactional
    public void deleteEmptyDocument(Document document) {
        Set<Quiz> quizzes = document.getQuizzes();
        if (quizzes == null || quizzes.isEmpty()) {
            documentRepository.delete(document);
        }
    }
}

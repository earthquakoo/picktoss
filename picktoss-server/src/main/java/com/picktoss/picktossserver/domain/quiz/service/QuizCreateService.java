package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.dto.response.CreateQuizSetResponse;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.global.enums.quiz.DailyQuizType;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_COUNT_EXCEEDED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizCreateService {

    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CreateQuizSetResponse createQuizSet(Long documentId, Long memberId, Integer quizCount, DailyQuizType dailyQuizType) {
        Member member = null;
        if (memberId != null) {
            member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        }

        List<Quiz> quizzes;
        if (dailyQuizType == DailyQuizType.ALL) {
            quizzes = quizRepository.findAllByDocumentId(documentId);
        } else {
            QuizType quizType = QuizType.valueOf(dailyQuizType.toString());
            quizzes = quizRepository.findAllByDocumentIdAndQuizType(documentId, quizType);
        }

        Document document = quizzes.getFirst().getDocument();
        document.updateDocumentTryCountBySolvedQuizSet();

        Collections.shuffle(quizzes);

        if (quizCount > quizzes.size()) {
            throw new CustomException(QUIZ_COUNT_EXCEEDED);
        }

        quizzes = quizzes.subList(0, quizCount);

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        String quizSetName = quizzes.getFirst().getDocument().getName();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetName, member);

        for (Quiz quiz : quizzes) {
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return CreateQuizSetResponse.builder()
                .quizSetId(quizSet.getId())
                .build();
    }
}

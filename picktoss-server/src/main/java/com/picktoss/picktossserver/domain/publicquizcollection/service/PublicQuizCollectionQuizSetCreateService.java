package com.picktoss.picktossserver.domain.publicquizcollection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionBookmarkRepository;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionRepository;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicQuizCollectionQuizSetCreateService {

    private final MemberRepository memberRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;
    private final PublicQuizCollectionRepository publicQuizCollectionRepository;
    private final PublicQuizCollectionBookmarkRepository publicQuizCollectionBookmarkRepository;

    @Transactional
    public void createPublicQuizCollectionQuizSet(Long memberId, Long publicQuizCollectionId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        PublicQuizCollection publicQuizCollection = publicQuizCollectionRepository.findById(publicQuizCollectionId)
                .orElseThrow(() -> new CustomException(ErrorInfo.PUBLIC_QUIZ_SET_NOT_FOUND));

        Document document = publicQuizCollection.getDocument();

        Set<Quiz> quizzes = document.getQuizzes();

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, document.getName(), QuizSetType.PUBLIC_QUIZ_COLLECTION, member);

        for (Quiz quiz : quizzes) {
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        publicQuizCollection.updateTryCountByPublicQuizCollectionSet();

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);
    }


    private static String createQuizSetId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

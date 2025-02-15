package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionCreateService {

    private final CollectionRepository collectionRepository;
    private final CollectionQuizRepository collectionQuizRepository;
    private final QuizRepository quizRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createCollection(List<Long> quizIds, String name, String description, String emoji, CollectionCategory collectionCategory, Long memberId) {
        List<Quiz> quizzes = quizRepository.findAllByMemberIdAndQuizIds(memberId, quizIds);
        if (quizzes.isEmpty()) {
            throw new CustomException(QUIZ_NOT_FOUND_ERROR);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        List<CollectionQuiz> collectionQuizzes = new ArrayList<>();

        Collection collection = Collection.createCollection(name, emoji, description, collectionCategory, member);

        for (Quiz quiz : quizzes) {
            CollectionQuiz collectionQuiz = CollectionQuiz.createQuizCollection(quiz, collection);
            collectionQuizzes.add(collectionQuiz);
        }

        collectionRepository.save(collection);
        collectionQuizRepository.saveAll(collectionQuizzes);
        return collection.getId();
    }
}

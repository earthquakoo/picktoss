package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSet;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSetCollectionQuiz;
import com.picktoss.picktossserver.domain.collection.repository.*;
import com.picktoss.picktossserver.domain.collection.util.CollectionUtil;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionUpdateService {

    private final CollectionRepository collectionRepository;
    private final CollectionQuizRepository collectionQuizRepository;
    private final CollectionQuizSetRepository collectionQuizSetRepository;
    private final CollectionBookmarkRepository collectionBookmarkRepository;
    private final CollectionComplaintRepository collectionComplaintRepository;
    private final CollectionComplaintFileRepository collectionComplaintFileRepository;
    private final QuizRepository quizRepository;
    private final MemberRepository memberRepository;
    private final CollectionUtil collectionUtil;
    private final StarHistoryRepository starHistoryRepository;

    @Transactional
    public void updateCollectionInfo(
            Long collectionId, Long memberId, String name, String description, String emoji, CollectionCategory collectionCategory) {
        Collection collection = collectionRepository.findCollectionByCollectionIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        collection.updateCollectionByUpdateCollectionInfo(name, description, emoji, collectionCategory);
    }

    @Transactional
    public void addQuizToCollection(Long collectionId, Long memberId, Long quizId) {
        Quiz quiz = quizRepository.findByQuizIdAndMemberId(quizId, memberId)
                .orElseThrow(() -> new CustomException(QUIZ_NOT_FOUND_ERROR));

        Collection collection = collectionRepository.findCollectionByCollectionIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
        for (CollectionQuiz collectionQuiz : collectionQuizzes) {
            if (Objects.equals(collectionQuiz.getQuiz().getId(), quiz.getId())) {
                throw new CustomException(DUPLICATE_QUIZ_IN_COLLECTION);
            }
        }

        CollectionQuiz collectionQuiz = CollectionQuiz.createQuizCollection(quiz, collection);
        collectionQuizRepository.save(collectionQuiz);
    }

    @Transactional
    public void updateCollectionQuizzes(List<Long> quizIds, Long collectionId, Long memberId) {
        List<Quiz> quizzes = quizRepository.findAllByMemberIdAndQuizIds(memberId, quizIds);
        if (quizzes.isEmpty()) {
            throw new CustomException(QUIZ_NOT_FOUND_ERROR);
        }

        Collection collection = collectionRepository.findCollectionByCollectionIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        Set<CollectionQuiz> curCollectionQuizzes = collection.getCollectionQuizzes();
        collectionQuizRepository.deleteAll(curCollectionQuizzes);

        List<CollectionQuiz> newCollectionQuizzes = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            CollectionQuiz collectionQuiz = CollectionQuiz.createQuizCollection(quiz, collection);
            newCollectionQuizzes.add(collectionQuiz);
        }

        collectionQuizRepository.saveAll(newCollectionQuizzes);
    }
}

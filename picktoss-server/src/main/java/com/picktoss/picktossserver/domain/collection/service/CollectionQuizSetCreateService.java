package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSet;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSetCollectionQuiz;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizSetCollectionQuizRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizSetRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.dto.response.CreateQuizzesResponse;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.COLLECTION_NOT_FOUND;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionQuizSetCreateService {

    private final CollectionRepository collectionRepository;
    private final CollectionQuizSetRepository collectionQuizSetRepository;
    private final CollectionQuizSetCollectionQuizRepository collectionQuizSetCollectionQuizRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CreateQuizzesResponse createCollectionQuizSet(Long collectionId, Long memberId) {
        Collection collection = collectionRepository.findCollectionWithCollectionQuizByCollectionId(collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
        String quizSetName = collection.getName();

        List<CollectionQuizSetCollectionQuiz> quizSetQuizzes = new ArrayList<>();
        String quizSetId = createQuizSetId();
        CollectionQuizSet collectionQuizSet = CollectionQuizSet.createCollectionQuizSet(quizSetId, quizSetName, QuizSetType.COLLECTION_QUIZ_SET, member);

        for (CollectionQuiz collectionQuiz : collectionQuizzes) {
            CollectionQuizSetCollectionQuiz collectionQuizSetCollectionQuiz = CollectionQuizSetCollectionQuiz.createCollectionQuizSetCollectionQuiz(collectionQuiz, collectionQuizSet);
            quizSetQuizzes.add(collectionQuizSetCollectionQuiz);
        }

        collectionQuizSetRepository.save(collectionQuizSet);
        collectionQuizSetCollectionQuizRepository.saveAll(quizSetQuizzes);

        return new CreateQuizzesResponse(quizSetId, QuizSetType.COLLECTION_QUIZ_SET, LocalDateTime.now());
    }

    private static String createQuizSetId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

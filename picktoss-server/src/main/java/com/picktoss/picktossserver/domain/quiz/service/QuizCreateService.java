package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.dto.response.CreateQuizzesResponse;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizCreateService {

    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;
    private final MemberRepository memberRepository;
    private final CollectionRepository collectionRepository;

    @Transactional
    public CreateQuizzesResponse createMemberGeneratedQuizSet(Long documentId, Long memberId, String stringQuizType, Integer quizCount) {
        List<Quiz> quizzes = quizRepository.findAllByDocumentIdAndMemberId(documentId, memberId);

        Member member = quizzes.getFirst().getDocument().getDirectory().getMember();
        String quizSetName = quizzes.getFirst().getDocument().getName();

        if (stringQuizType.equals("RANDOM")) {
            Collections.shuffle(quizzes);
        } else {
            QuizType quizType = QuizType.valueOf(stringQuizType);
            quizzes = quizzes.stream()
                    .filter(quiz -> quiz.getQuizType() == quizType)
                    .toList();
            if (quizzes.isEmpty()) {
                throw new CustomException(QUIZ_TYPE_NOT_IN_DOCUMENT);
            }
        }

        if (quizCount > quizzes.size()) {
            throw new CustomException(QUIZ_COUNT_EXCEEDED);
        }

        quizzes = quizzes.subList(0, quizCount);

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, quizSetName, QuizSetType.DOCUMENT_QUIZ_SET, member);

        for (Quiz quiz : quizzes) {
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return new CreateQuizzesResponse(quizSetId, QuizSetType.DOCUMENT_QUIZ_SET, LocalDateTime.now());
    }

    @Transactional
    public CreateQuizzesResponse createErrorCheckQuizSet(Long documentId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        List<Quiz> quizzes = quizRepository.findAllByDocumentIdAndIsLatestAndMemberId(documentId, memberId);
        String quizSetName = quizzes.getFirst().getDocument().getName();

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, quizSetName, QuizSetType.FIRST_QUIZ_SET, member);

        for (Quiz quiz : quizzes) {
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return new CreateQuizzesResponse(quizSetId, QuizSetType.FIRST_QUIZ_SET, LocalDateTime.now());
    }

    @Transactional
    public CreateQuizzesResponse createCollectionQuizSet(Long collectionId, Long memberId) {
        Collection collection = collectionRepository.findCollectionWithCollectionQuizByCollectionId(collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
        String quizSetName = collection.getName();

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, quizSetName, QuizSetType.COLLECTION_QUIZ_SET, member);

        for (CollectionQuiz collectionQuiz : collectionQuizzes) {
            Quiz quiz = collectionQuiz.getQuiz();
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return new CreateQuizzesResponse(quizSetId, QuizSetType.COLLECTION_QUIZ_SET, LocalDateTime.now());
    }

    private static String createQuizSetId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

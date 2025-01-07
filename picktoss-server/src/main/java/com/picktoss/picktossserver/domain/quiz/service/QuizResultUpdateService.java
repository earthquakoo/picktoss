package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSet;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSetCollectionQuiz;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizSetRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.constant.QuizConstant;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.dto.request.UpdateRandomQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.dto.response.UpdateQuizResultResponse;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.entity.RandomQuizRecord;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.domain.quiz.repository.RandomQuizRecordRepository;
import com.picktoss.picktossserver.domain.quiz.util.QuizUtil;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_SET_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizResultUpdateService {

    private final QuizUtil quizUtil;
    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;
    private final RandomQuizRecordRepository randomQuizRecordRepository;
    private final CollectionQuizSetRepository collectionQuizSetRepository;

    @Transactional
    public UpdateQuizResultResponse updateQuizResult(List<UpdateQuizResultRequest.UpdateQuizResultQuizDto> quizDtos, String quizSetId, QuizSetType quizSetType, Long memberId) {
        if (quizSetType == QuizSetType.COLLECTION_QUIZ_SET) {
            return updateQuizResultByCollectionQuizSet(quizDtos, quizSetId, quizSetType, memberId);
        }
        return updateQuizResultByQuizSet(quizDtos, quizSetId, quizSetType, memberId);
    }

    @Transactional
    public void updateRandomQuizResult(List<UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto> quizDtos, Long memberId) {
        List<Long> quizIds = new ArrayList<>();
        for (UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto quizDto : quizDtos) {
            quizIds.add(quizDto.getId());
        }

        List<Quiz> quizzes = quizRepository.findAllByMemberIdAndQuizIds(memberId, quizIds);
        Member member = quizzes.getFirst().getDocument().getDirectory().getMember();

        RandomQuizRecord todayRandomQuizRecord = findTodayRandomQuizRecordByMemberIdAndCreatedAtBetween(member);

        Map<Long, Quiz> quizMap = quizzes.stream()
                .collect(Collectors.toMap(Quiz::getId, Function.identity()));

        for (UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto quizDto : quizDtos) {
            Quiz quiz = quizMap.get(quizDto.getId());
            if (quiz != null) {
                if (quizDto.isAnswer()) {
                    quiz.updateIsReviewNeededFalseByCorrectAnswer();
                    todayRandomQuizRecord.updateQuizCountByCorrectAnswer();
                } else {
                    quiz.updateIsReviewNeededTrueByIncorrectAnswer();
                    todayRandomQuizRecord.updateQuizCountByIncorrectAnswer();
                }
            }
        }
    }

    @Transactional
    public void updateWrongQuizResult(List<UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto> quizDtos, Long memberId) {
        List<Long> quizIds = new ArrayList<>();
        for (UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto quizDto : quizDtos) {
            quizIds.add(quizDto.getId());
        }

        List<Quiz> quizzes = quizRepository.findAllByMemberIdAndQuizIds(memberId, quizIds);
        Map<Long, Quiz> quizMap = quizzes.stream()
                .collect(Collectors.toMap(Quiz::getId, Function.identity()));

        for (UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto quizDto : quizDtos) {
            Quiz quiz = quizMap.get(quizDto.getId());
            if (quiz == null) continue;

            if (quizDto.isAnswer()) {
                quiz.updateIsReviewNeededFalseByCorrectAnswer();
            } else {
                quiz.updateIsReviewNeededTrueByIncorrectAnswer();
            }
        }
    }

    private UpdateQuizResultResponse updateQuizResultByCollectionQuizSet(List<UpdateQuizResultRequest.UpdateQuizResultQuizDto> quizDtos, String quizSetId, QuizSetType quizSetType, Long memberId) {
        CollectionQuizSet collectionQuizSet = collectionQuizSetRepository.findCollectionQuizSetByMemberIdAndQuizSetId(memberId, quizSetId)
                .orElseThrow(() -> new CustomException(QUIZ_SET_NOT_FOUND_ERROR));

        List<CollectionQuizSetCollectionQuiz> collectionQuizSetCollectionQuizzes = collectionQuizSet.getCollectionQuizSetCollectionQuizzes();

        Map<Long, UpdateQuizResultRequest.UpdateQuizResultQuizDto> quizDtoMap = new HashMap<>();
        for (UpdateQuizResultRequest.UpdateQuizResultQuizDto quizDto : quizDtos) {
            quizDtoMap.put(quizDto.getId(), quizDto);
        }

        int totalQuizCount = 0;
        int totalElapsedTime = 0;
        int correctAnswerCount = 0;

        for (CollectionQuizSetCollectionQuiz collectionQuizSetCollectionQuiz : collectionQuizSetCollectionQuizzes) {
            Quiz quiz = collectionQuizSetCollectionQuiz.getCollectionQuiz().getQuiz();
            if (quizDtoMap.containsKey(quiz.getId())) {
                UpdateQuizResultRequest.UpdateQuizResultQuizDto quizDto = quizDtoMap.get(quiz.getId());

                collectionQuizSetCollectionQuiz.updateIsAnswer(quizDto.isAnswer());
                collectionQuizSetCollectionQuiz.updateChoseAnswer(quizDto.getChoseAnswer());
                collectionQuizSetCollectionQuiz.updateElapsedTime(quizDto.getElapsedTime());

                if (quizDto.isAnswer()) {
                    correctAnswerCount += 1;
                }
                totalQuizCount += 1;
                totalElapsedTime += quizDto.getElapsedTime();
            }
        }
        collectionQuizSet.updateSolved();

        double correctAnswerRate = (double) correctAnswerCount / (double) totalQuizCount * 100.0;
        return new UpdateQuizResultResponse(totalQuizCount, totalElapsedTime, correctAnswerRate, null, null);
    }

    private UpdateQuizResultResponse updateQuizResultByQuizSet(List<UpdateQuizResultRequest.UpdateQuizResultQuizDto> quizDtos, String quizSetId, QuizSetType quizSetType, Long memberId) {
        QuizSet quizSet = quizSetRepository.findQuizSetByMemberIdAndQuizSetId(memberId, quizSetId)
                .orElseThrow(() -> new CustomException(QUIZ_SET_NOT_FOUND_ERROR));

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
            if (quizDtoMap.containsKey(quiz.getId())) {
                UpdateQuizResultRequest.UpdateQuizResultQuizDto quizDto = quizDtoMap.get(quiz.getId());

                if (!quizDto.isAnswer()) {
                    quiz.updateIsReviewNeededTrueByIncorrectAnswer();
                } else {
                    quiz.addCorrectAnswerCount();
                    correctAnswerCount += 1;
                }
                quizSetQuiz.updateIsAnswer(quizDto.isAnswer());
                quizSetQuiz.updateChoseAnswer(quizDto.getChoseAnswer());
                quizSetQuiz.updateElapsedTime(quizDto.getElapsedTime());

                totalQuizCount += 1;
                totalElapsedTime += quizDto.getElapsedTime();
            }
        }
        quizSet.updateSolved();

        double correctAnswerRate = (double) correctAnswerCount / (double) totalQuizCount * 100.0;


        if (quizSetType != QuizSetType.FIRST_QUIZ_SET) {
            List<QuizSet> quizSets = quizSetRepository.findAllByMemberIdAndSolvedTrue(memberId);
            int currentConsecutiveTodayQuizDate = quizUtil.checkCurrentConsecutiveSolvedQuizSet(quizSets);
            if (currentConsecutiveTodayQuizDate % 5 == 0) {
                return new UpdateQuizResultResponse(totalQuizCount, totalElapsedTime, correctAnswerRate, QuizConstant.FIVE_DAYS_CONSECUTIVE_REWARD, currentConsecutiveTodayQuizDate);
            }
            return new UpdateQuizResultResponse(totalQuizCount, totalElapsedTime, correctAnswerRate, QuizConstant.ONE_DAYS_REWARD, currentConsecutiveTodayQuizDate);
        }
        return new UpdateQuizResultResponse(totalQuizCount, totalElapsedTime, correctAnswerRate, null, null);
    }

    private RandomQuizRecord findTodayRandomQuizRecordByMemberIdAndCreatedAtBetween(Member member) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Optional<RandomQuizRecord> optionalRandomQuizRecord = randomQuizRecordRepository.findByMemberIdAndCreatedAtBetween(member.getId(), startOfDay, endOfDay);

        if (optionalRandomQuizRecord.isEmpty()) {
            RandomQuizRecord randomQuizRecord = RandomQuizRecord.createRandomQuizRecord(member);
            randomQuizRecordRepository.save(randomQuizRecord);
            return randomQuizRecord;
        } else {
            return optionalRandomQuizRecord.get();
        }
    }
}

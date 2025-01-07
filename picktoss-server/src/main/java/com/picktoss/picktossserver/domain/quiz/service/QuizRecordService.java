package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSet;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSetCollectionQuiz;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizSetCollectionQuizRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizSetRepository;
import com.picktoss.picktossserver.domain.quiz.dto.dto.SolvedQuizRecordDto;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizRecordsResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetSingleQuizRecordByDateResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetSingleQuizSetRecordResponse;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.domain.quiz.util.QuizUtil;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizRecordService {

    private final QuizUtil quizUtil;
    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;
    private final CollectionQuizSetRepository collectionQuizSetRepository;
    private final CollectionQuizSetCollectionQuizRepository collectionQuizSetCollectionQuizRepository;

    public GetSingleQuizRecordByDateResponse findAllQuizSetRecordByDate(Long memberId, LocalDate solvedDate) {
        List<QuizSet> solvedQuizSets = quizSetRepository.findAllByMemberIdAndSolvedTrue(memberId);
        int currentConsecutiveDays = quizUtil.checkCurrentConsecutiveSolvedQuizSet(solvedQuizSets);
        int maxConsecutiveDays = quizUtil.checkMaxConsecutiveSolvedQuizSet(solvedQuizSets);

        LocalDateTime startDateTime = solvedDate.atStartOfDay();
        LocalDateTime endDateTime = solvedDate.atTime(LocalTime.MAX);

        List<QuizSet> quizSets = quizSetRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, startDateTime, endDateTime);
        List<CollectionQuizSet> collectionQuizSets = collectionQuizSetRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, startDateTime, endDateTime);

        List<GetQuizRecordsResponse.GetQuizRecordsDto> quizRecordsDtos = quizSetsToRecordDtos(quizSets, collectionQuizSets);

        return new GetSingleQuizRecordByDateResponse(currentConsecutiveDays, maxConsecutiveDays, quizRecordsDtos);
    }

    public GetQuizRecordsResponse findAllQuizAndCollectionRecords(Long memberId) {
        List<QuizSet> solvedQuizSets = quizSetRepository.findAllByMemberIdAndSolvedTrue(memberId);
        List<CollectionQuizSet> collectionQuizSets = collectionQuizSetRepository.findAllByMemberIdAndSolvedTrue(memberId);
        int currentConsecutiveDays = quizUtil.checkCurrentConsecutiveSolvedQuizSet(solvedQuizSets);
        int maxConsecutiveDays = quizUtil.checkMaxConsecutiveSolvedQuizSet(solvedQuizSets);

        List<SolvedQuizRecordDto> solvedQuizRecords = new ArrayList<>();

        for (QuizSet quizSet : solvedQuizSets) {
            List<QuizSetQuiz> quizSetQuizzes = quizSet.getQuizSetQuizzes();
            int quizCount = quizSetQuizzes.size();
            int score = quizCount;
            for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
                if (!quizSetQuiz.getIsAnswer()) {
                    score -= 1;
                }
            }

            solvedQuizRecords.add(SolvedQuizRecordDto.builder()
                    .solvedDate(quizSet.getCreatedAt().toLocalDate())
                    .id(quizSet.getId())
                    .name(quizSet.getName())
                    .quizCount(quizCount)
                    .score(score)
                    .quizSettype(quizSet.getQuizSetType())
                    .build());
        }

        for (CollectionQuizSet collectionQuizSet : collectionQuizSets) {
            List<CollectionQuizSetCollectionQuiz> collectionQuizSetCollectionQuizzes = collectionQuizSet.getCollectionQuizSetCollectionQuizzes();
            int quizCount = collectionQuizSetCollectionQuizzes.size();
            int score = quizCount;

            for (CollectionQuizSetCollectionQuiz collectionQuizSetCollectionQuiz : collectionQuizSetCollectionQuizzes) {
                if (!collectionQuizSetCollectionQuiz.getIsAnswer()) {
                    score -= 1;
                }
            }

            solvedQuizRecords.add(SolvedQuizRecordDto.builder()
                    .solvedDate(collectionQuizSet.getCreatedAt().toLocalDate())
                    .id(collectionQuizSet.getId())
                    .name(collectionQuizSet.getName())
                    .quizCount(quizCount)
                    .score(score)
                    .quizSettype(collectionQuizSet.getQuizSetType())
                    .build());
        }

        Map<LocalDate, List<SolvedQuizRecordDto>> dateQuizRecordsMap = solvedQuizRecords.stream()
                .collect(Collectors.groupingBy(SolvedQuizRecordDto::getSolvedDate));

        List<GetQuizRecordsResponse.GetQuizRecordsSolvedDateDto> quizRecordSolvedDateDtos = new ArrayList<>();
        List<LocalDate> sortedDates = new ArrayList<>(dateQuizRecordsMap.keySet());
        sortedDates.sort(Comparator.reverseOrder());

        for (LocalDate solvedDate : sortedDates) {
            List<SolvedQuizRecordDto> records = dateQuizRecordsMap.get(solvedDate);

            List<GetQuizRecordsResponse.GetQuizRecordsDto> quizRecordDtos = records.stream()
                    .map(record -> GetQuizRecordsResponse.GetQuizRecordsDto.builder()
                            .quizSetId(record.getId())
                            .quizCount(record.getQuizCount())
                            .name(record.getName())
                            .score(record.getScore())
                            .quizSetType(record.getQuizSettype())
                            .build())
                    .collect(Collectors.toList());

            quizRecordSolvedDateDtos.add(
                    GetQuizRecordsResponse.GetQuizRecordsSolvedDateDto.builder()
                            .solvedDate(solvedDate)
                            .quizRecords(quizRecordDtos)
                            .build());
        }
        return new GetQuizRecordsResponse(currentConsecutiveDays, maxConsecutiveDays, quizRecordSolvedDateDtos);
    }

    public GetSingleQuizSetRecordResponse findQuizSetRecordByMemberIdAndQuizSetId(Long memberId, String quizSetId, QuizSetType quizSetType) {
        if (quizSetType == QuizSetType.COLLECTION_QUIZ_SET) {
            return collectionQuizSetRecordResponse(memberId, quizSetId, quizSetType);
        }
        return quizSetRecordResponse(memberId, quizSetId, quizSetType);
    }

    private List<GetQuizRecordsResponse.GetQuizRecordsDto> quizSetsToRecordDtos(List<QuizSet> quizSets, List<CollectionQuizSet> collectionQuizSets) {
        List<GetQuizRecordsResponse.GetQuizRecordsDto> quizRecordsDtos = new ArrayList<>();

        for (QuizSet quizSet : quizSets) {
            int quizCount = quizSet.getQuizSetQuizzes().size();
            int score = quizCount;
            List<QuizSetQuiz> quizSetQuizzes = quizSet.getQuizSetQuizzes();
            for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
                if (!quizSetQuiz.getIsAnswer()) {
                    score -= 1;
                }
            }

            GetQuizRecordsResponse.GetQuizRecordsDto quizRecordsDto = GetQuizRecordsResponse.GetQuizRecordsDto.builder()
                    .quizSetId(quizSet.getId())
                    .quizCount(quizCount)
                    .name(quizSet.getName())
                    .score(score)
                    .quizSetType(quizSet.getQuizSetType())
                    .build();

            quizRecordsDtos.add(quizRecordsDto);
        }

        for (CollectionQuizSet collectionQuizSet : collectionQuizSets) {
            int quizCount = collectionQuizSet.getCollectionQuizSetCollectionQuizzes().size();
            int score = quizCount;

            List<CollectionQuizSetCollectionQuiz> collectionQuizSetCollectionQuizzes = collectionQuizSet.getCollectionQuizSetCollectionQuizzes();
            for (CollectionQuizSetCollectionQuiz collectionQuizSetCollectionQuiz : collectionQuizSetCollectionQuizzes) {
                if (!collectionQuizSetCollectionQuiz.getIsAnswer()) {
                    score -= 1;
                }
            }
            GetQuizRecordsResponse.GetQuizRecordsDto quizRecordsDto = GetQuizRecordsResponse.GetQuizRecordsDto.builder()
                    .quizSetId(collectionQuizSet.getId())
                    .quizCount(quizCount)
                    .name(collectionQuizSet.getName())
                    .score(score)
                    .quizSetType(collectionQuizSet.getQuizSetType())
                    .build();

            quizRecordsDtos.add(quizRecordsDto);
        }
        return quizRecordsDtos;
    }

    private GetSingleQuizSetRecordResponse collectionQuizSetRecordResponse(Long memberId, String quizSetId, QuizSetType quizSetType) {
        List<GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto> quizSetRecordDtos = new ArrayList<>();

        List<CollectionQuizSetCollectionQuiz> collectionQuizSetCollectionQuizzes = collectionQuizSetCollectionQuizRepository.findAllByQuizSetIdAndMemberId(quizSetId, memberId);
        int elapsedTimeMs = 0;
        for (CollectionQuizSetCollectionQuiz collectionQuizSetCollectionQuiz : collectionQuizSetCollectionQuizzes) {
            Quiz quiz = collectionQuizSetCollectionQuiz.getCollectionQuiz().getQuiz();

            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                if (options.isEmpty()) {
                    continue;
                }
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto quizSetRecordDto = GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .quizType(quiz.getQuizType())
                    .options(optionList)
                    .choseAnswer(collectionQuizSetCollectionQuiz.getChoseAnswer())
                    .isAnswer(collectionQuizSetCollectionQuiz.getIsAnswer())
                    .documentName(quiz.getDocument().getName())
                    .directoryName(quiz.getDocument().getDirectory().getName())
                    .quizSetType(quizSetType)
                    .build();

            quizSetRecordDtos.add(quizSetRecordDto);
        }

        LocalDateTime createdAt = collectionQuizSetCollectionQuizzes.getFirst().getCollectionQuizSet().getCreatedAt();

        return new GetSingleQuizSetRecordResponse(elapsedTimeMs, quizSetRecordDtos, createdAt);
    }

    private GetSingleQuizSetRecordResponse quizSetRecordResponse(Long memberId, String quizSetId, QuizSetType quizSetType) {
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByQuizSetIdAndMemberId(quizSetId, memberId);
        List<GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto> quizSetRecordDtos = new ArrayList<>();

        int elapsedTimeMs = 0;
        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {

            elapsedTimeMs += quizSetQuiz.getElapsedTimeMs();
            Quiz quiz = quizSetQuiz.getQuiz();

            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                if (options.isEmpty()) {
                    continue;
                }
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto quizSetRecordDto = GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .quizType(quiz.getQuizType())
                    .options(optionList)
                    .choseAnswer(quizSetQuiz.getChoseAnswer())
                    .isAnswer(quizSetQuiz.getIsAnswer())
                    .collectionName(quizSetQuiz.getQuizSet().getName())
                    .quizSetType(quizSetType)
                    .build();

            quizSetRecordDtos.add(quizSetRecordDto);
        }

        LocalDateTime createdAt = quizSetQuizzes.getFirst().getQuizSet().getCreatedAt();

        return new GetSingleQuizSetRecordResponse(elapsedTimeMs, quizSetRecordDtos, createdAt);
    }
}

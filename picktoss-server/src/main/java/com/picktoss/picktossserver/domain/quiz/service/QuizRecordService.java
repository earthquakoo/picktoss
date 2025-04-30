package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetAllQuizRecordsResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetConsecutiveSolvedDailyQuizDatesResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetSingleDailyQuizRecordResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetSingleQuizSetRecordResponse;
import com.picktoss.picktossserver.domain.quiz.entity.*;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordDetailRepository;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizRecordService {

    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;
    private final DailyQuizRecordRepository dailyQuizRecordRepository;
    private final DailyQuizRecordDetailRepository dailyQuizRecordDetailRepository;

    public GetAllQuizRecordsResponse findAllQuizRecords(Long memberId) {
        List<QuizSet> quizSets = quizSetRepository.findAllByMemberIdAndSolvedTrueOrderByCreatedAtDesc(memberId);

        List<GetAllQuizRecordsResponse.GetAllQuizRecordQuizSetDto> quizSetDtos = new ArrayList<>();
        for (QuizSet quizSet : quizSets) {
            String documentName = quizSet.getQuizSetQuizzes().getFirst().getQuiz().getDocument().getName();

            GetAllQuizRecordsResponse.GetAllQuizRecordQuizSetDto quizSetDto = GetAllQuizRecordsResponse.GetAllQuizRecordQuizSetDto.builder()
                    .quizSetId(quizSet.getId())
                    .quizSetName(documentName)
                    .totalQuizCount(quizSet.getQuizSetQuizzes().size())
                    .createdAt(quizSet.getCreatedAt())
                    .build();

            quizSetDtos.add(quizSetDto);
        }

        List<DailyQuizRecord> dailyQuizRecords = dailyQuizRecordRepository.findAllByMemberIdOrderBySolvedDateDesc(memberId);

        List<GetAllQuizRecordsResponse.GetAllQuizRecordDailyQuizDto> dailyQuizDtos = new ArrayList<>();
        for (DailyQuizRecord dailyQuizRecord : dailyQuizRecords) {

            GetAllQuizRecordsResponse.GetAllQuizRecordDailyQuizDto dailyQUizDto = GetAllQuizRecordsResponse.GetAllQuizRecordDailyQuizDto.builder()
                    .dailyQuizRecordId(dailyQuizRecord.getId())
                    .totalQuizCount(dailyQuizRecord.getDailyQuizRecordDetails().size())
                    .solvedDate(dailyQuizRecord.getSolvedDate())
                    .build();

            dailyQuizDtos.add(dailyQUizDto);
        }
        return new GetAllQuizRecordsResponse(quizSetDtos, dailyQuizDtos);
    }

    public GetSingleQuizSetRecordResponse findSingleQuizSetRecord(Long memberId, Long quizSetId) {
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByQuizSetIdAndMemberIdAndSolvedTrue(quizSetId, memberId);
        if (quizSetQuizzes == null || quizSetQuizzes.isEmpty()) {
            throw new CustomException(ErrorInfo.UNRESOLVED_QUIZ_SET);
        }

        List<GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto> quizSetRecordDtos = new ArrayList<>();

        int elapsedTimeMs = 0;
        int correctAnswerCount = 0;
        int totalQuizCount = quizSetQuizzes.size();
        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {

            if (!Objects.isNull(quizSetQuiz.getIsAnswer()) && quizSetQuiz.getIsAnswer()) {
                correctAnswerCount += 1;
            }

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
                    .build();

            quizSetRecordDtos.add(quizSetRecordDto);
        }

        double averageCorrectRate = (double) correctAnswerCount / (double) totalQuizCount * 100.0;

        LocalDateTime createdAt = quizSetQuizzes.getFirst().getQuizSet().getCreatedAt();

        return new GetSingleQuizSetRecordResponse(totalQuizCount, elapsedTimeMs, averageCorrectRate, createdAt, quizSetRecordDtos);
    }

    public GetSingleDailyQuizRecordResponse findSingleDailyQuizRecord(Long dailyQuizRecordId, Long memberId) {
        List<DailyQuizRecordDetail> dailyQuizRecordDetails = dailyQuizRecordDetailRepository.findAllByDailyQuizRecordIdAndMemberId(dailyQuizRecordId, memberId);

        List<GetSingleDailyQuizRecordResponse.GetSingleDailyQuizRecordDto> quizDtos = new ArrayList<>();
        for (DailyQuizRecordDetail dailyQuizRecordDetail : dailyQuizRecordDetails) {
            Quiz quiz = dailyQuizRecordDetail.getQuiz();

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

            GetSingleDailyQuizRecordResponse.GetSingleDailyQuizRecordDto quizDto = GetSingleDailyQuizRecordResponse.GetSingleDailyQuizRecordDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .quizType(quiz.getQuizType())
                    .options(optionList)
                    .choseAnswer(dailyQuizRecordDetail.getChoseAnswer())
                    .isAnswer(dailyQuizRecordDetail.getIsAnswer())
                    .build();

            quizDtos.add(quizDto);
        }
        return new GetSingleDailyQuizRecordResponse(quizDtos);
    }

    public GetConsecutiveSolvedDailyQuizDatesResponse findConsecutiveSolvedQuizSetDates(Long memberId, LocalDate solvedDate) {
        List<DailyQuizRecord> dailyQuizRecords = dailyQuizRecordRepository.findAllByMemberIdAndIsDailyQuizCompleteTrueAndSolvedDateOrderBySolvedDate(memberId, solvedDate);

        HashMap<LocalDate, Boolean> solvedDailyQuizByDate = new LinkedHashMap<>();
        List<GetConsecutiveSolvedDailyQuizDatesResponse.GetDailyQuizRecordByDateDto> solvedQuizDateRecords = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(solvedDate.getYear(), solvedDate.getMonth());
        LocalDate startOfDate = yearMonth.atDay(1);
        LocalDate endOfDate = yearMonth.atEndOfMonth();

        for (int i = 0; i <= endOfDate.getDayOfMonth() - startOfDate.getDayOfMonth(); i++) {
            LocalDate date = startOfDate.plusDays(i);
            solvedDailyQuizByDate.put(date, false);
        }

        for (DailyQuizRecord dailyQuizRecord : dailyQuizRecords) {
            LocalDate date = dailyQuizRecord.getSolvedDate();
            solvedDailyQuizByDate.put(date, true);
        }

        for (LocalDate localDate : solvedDailyQuizByDate.keySet()) {
            Boolean isDailyQuizComplete = solvedDailyQuizByDate.get(localDate);
            GetConsecutiveSolvedDailyQuizDatesResponse.GetDailyQuizRecordByDateDto quizRecordByDateDto = GetConsecutiveSolvedDailyQuizDatesResponse.GetDailyQuizRecordByDateDto.builder()
                    .date(localDate)
                    .isDailyQuizComplete(isDailyQuizComplete)
                    .build();

            solvedQuizDateRecords.add(quizRecordByDateDto);
        }

        return new GetConsecutiveSolvedDailyQuizDatesResponse(solvedQuizDateRecords);
    }

    public int calculateConsecutiveDailyQuiz(Long memberId) {
        List<DailyQuizRecord> dailyQuizRecords = dailyQuizRecordRepository.findAllByMemberIdAndIsDailyQuizCompleteTrueOrderBySolvedDateDesc(memberId);
        if (dailyQuizRecords.isEmpty()) {
            return 0;
        }

        LocalDate firstQuizSetDate = dailyQuizRecords.getFirst().getSolvedDate();
        if (!firstQuizSetDate.equals(LocalDate.now()) && !firstQuizSetDate.equals(LocalDate.now().minusDays(1))) {
            return 0;
        }

        LocalDate previousDate = null;
        int currentConsecutiveDays = 0;

        for (DailyQuizRecord dailyQuizRecord : dailyQuizRecords) {
            LocalDate solvedDate = dailyQuizRecord.getSolvedDate();

            if (previousDate == null || previousDate.minusDays(1).equals(solvedDate)) {
                currentConsecutiveDays += 1;
            } else if (!previousDate.equals(solvedDate)) {
                break;
            }
            previousDate = solvedDate;
        }

        return currentConsecutiveDays;
    }
}

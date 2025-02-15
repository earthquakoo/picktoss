package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSet;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSetCollectionQuiz;
import com.picktoss.picktossserver.domain.collection.entity.CollectionRandomQuizRecord;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizSetRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRandomQuizRecordRepository;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizMonthlyAnalysisResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizWeeklyAnalysisResponse;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.entity.RandomQuizRecord;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.RandomQuizRecordRepository;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizAnalysisService {

    private final QuizSetQuizRepository quizSetQuizRepository;
    private final RandomQuizRecordRepository randomQuizRecordRepository;
    private final CollectionQuizSetRepository collectionQuizSetRepository;
    private final CollectionRandomQuizRecordRepository collectionRandomQuizRecordRepository;

    public GetQuizWeeklyAnalysisResponse findQuizWeeklyAnalysis(Long memberId, Long directoryId, LocalDate startDate, LocalDate endDate) {
        List<QuizSetQuiz> quizSetQuizzes;

        if (directoryId == null) {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndSolvedTrue(memberId);
        } else {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndDirectoryIdAndSolvedTrue(memberId, directoryId);
        }
        return quizWeeklyAnalysis(quizSetQuizzes, startDate, endDate, memberId);
    }

    public GetQuizMonthlyAnalysisResponse findQuizMonthlyAnalysis(Long memberId, Long directoryId, LocalDate startMonthDate) {
        List<QuizSetQuiz> quizSetQuizzes;

        if (directoryId == null) {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndSolvedTrue(memberId);
        } else {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndDirectoryIdAndSolvedTrue(memberId, directoryId);
        }
        return quizMonthlyAnalysis(quizSetQuizzes, startMonthDate, memberId);
    }

    private GetQuizWeeklyAnalysisResponse quizWeeklyAnalysis(
            List<QuizSetQuiz> quizSetQuizzes, LocalDate startDate, LocalDate endDate, Long memberId) {
        HashMap<LocalDate, Integer> correctAnswerCountByDate = new LinkedHashMap<>();
        HashMap<LocalDate, Integer> totalQuizCountByDate = new LinkedHashMap<>();

        long daysBetween = startDate.until(endDate, ChronoUnit.DAYS);
        for (int i = 0; i <= daysBetween; i++) {
            LocalDate date = startDate.plusDays(i);
            correctAnswerCountByDate.put(date, 0);
            totalQuizCountByDate.put(date, 0);
        }

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            LocalDate date = quizSetQuiz.getUpdatedAt().toLocalDate();

            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + 1);
                if (!Objects.isNull(quizSetQuiz.getIsAnswer()) && quizSetQuiz.getIsAnswer()) {
                    correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + 1);
                }
            }
        }

        LocalDateTime startOfDay = startDate.atStartOfDay();
        LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);

        List<RandomQuizRecord> randomQuizRecords = randomQuizRecordRepository.findAllByMemberIdAndCreatedAtBetween(memberId, startOfDay, endOfDay);
        for (RandomQuizRecord randomQuizRecord : randomQuizRecords) {
            LocalDate date = randomQuizRecord.getUpdatedAt().toLocalDate();

            if (!date.isBefore(startDate) && !date.isAfter(startDate.plusDays(7))) {
                int solvedQuizCount = randomQuizRecord.getSolvedQuizCount();
                int correctQuizCount = randomQuizRecord.getCorrectQuizCount();
                totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + solvedQuizCount);
                correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + correctQuizCount);
            }
        }

        List<CollectionRandomQuizRecord> collectionRandomQUizRecords = collectionRandomQuizRecordRepository.findAllByMemberIdAndCreatedAtBetween(memberId, startOfDay, endOfDay);
        for (CollectionRandomQuizRecord collectionRandomQUizRecord : collectionRandomQUizRecords) {
            LocalDate date = collectionRandomQUizRecord.getUpdatedAt().toLocalDate();

            if (!date.isBefore(startDate) && !date.isAfter(startDate.plusDays(7))) {
                int solvedQuizCount = collectionRandomQUizRecord.getSolvedQuizCount();
                int correctQuizCount = collectionRandomQUizRecord.getCorrectQuizCount();
                totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + solvedQuizCount);
                correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + correctQuizCount);
            }
        }

        Map<CollectionCategory, Integer> collectionFieldMap = new HashMap<>();
        List<CollectionQuizSet> collectionQuizSets = collectionQuizSetRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, startOfDay, endOfDay);

        for (CollectionQuizSet collectionQuizSet : collectionQuizSets) {
            LocalDate date = collectionQuizSet.getCreatedAt().toLocalDate();
            List<CollectionQuizSetCollectionQuiz> collectionQuizSetCollectionQuizzes = collectionQuizSet.getCollectionQuizSetCollectionQuizzes();
            int totalQuizCount = collectionQuizSetCollectionQuizzes.size();

            totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + totalQuizCount);
            for (CollectionQuizSetCollectionQuiz collectionQuizSetCollectionQuiz : collectionQuizSetCollectionQuizzes) {
                if (!Objects.isNull(collectionQuizSetCollectionQuiz.getIsAnswer()) && collectionQuizSetCollectionQuiz.getIsAnswer()) {
                    correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + 1);
                }

            }


            Collection collection = collectionQuizSet.getCollectionQuizSetCollectionQuizzes().getFirst().getCollectionQuiz().getCollection();
            collectionFieldMap.putIfAbsent(collection.getCollectionCategory(), totalQuizCount);
        }

        List<GetQuizWeeklyAnalysisResponse.QuizAnswerRateAnalysisDto> quizzesDtos = new ArrayList<>();
        int weeklyTotalQuizCount = 0;
        int weeklyCorrectAnswerCount = 0;

        for (LocalDate date : correctAnswerCountByDate.keySet()) {
            if (!correctAnswerCountByDate.isEmpty() && !totalQuizCountByDate.isEmpty()) {
                int totalQuizCount = totalQuizCountByDate.getOrDefault(date, 0);
                int correctAnswerCount = correctAnswerCountByDate.getOrDefault(date, 0);

                weeklyTotalQuizCount += totalQuizCount;
                weeklyCorrectAnswerCount += correctAnswerCount;

                GetQuizWeeklyAnalysisResponse.QuizAnswerRateAnalysisDto quizzesDto = GetQuizWeeklyAnalysisResponse.QuizAnswerRateAnalysisDto.builder()
                        .date(date)
                        .totalQuizCount(totalQuizCount)
                        .correctAnswerCount(correctAnswerCount)
                        .build();

                quizzesDtos.add(quizzesDto);
            }
        }

        int averageDailyQuizCount = weeklyTotalQuizCount / 7;

        double averageCorrectRate = (double) weeklyCorrectAnswerCount / (double) weeklyTotalQuizCount * 100.0;

        return new GetQuizWeeklyAnalysisResponse(quizzesDtos, averageDailyQuizCount, averageCorrectRate, weeklyTotalQuizCount, collectionFieldMap);
    }

    private GetQuizMonthlyAnalysisResponse quizMonthlyAnalysis(List<QuizSetQuiz> quizSetQuizzes, LocalDate startDate, Long memberId) {
        HashMap<LocalDate, Integer> correctAnswerCountByDate = new LinkedHashMap<>();
        HashMap<LocalDate, Integer> totalQuizCountByDate = new LinkedHashMap<>();
        HashMap<LocalDate, Integer> lastMonthTotalQuizCountDateMap = new LinkedHashMap<>();

        int monthlyTotalQuizCount = 0;
        int monthlyTotalCorrectAnswerCount = 0;

        YearMonth yearMonth = YearMonth.of(startDate.getYear(), startDate.getMonth());
        LocalDate startOfDate = yearMonth.atDay(1);
        LocalDate endOfDate = yearMonth.atEndOfMonth();

        // 전월 날짜 계산
        LocalDate currentDate = LocalDate.now();
        LocalDate lastMonthStart = startDate.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = startDate.minusMonths(1).withDayOfMonth(currentDate.getDayOfMonth());

        for (int i = 0; i <= endOfDate.getDayOfMonth() - startOfDate.getDayOfMonth(); i++) {
            LocalDate date = startOfDate.plusDays(i);
            correctAnswerCountByDate.put(date, 0);
            totalQuizCountByDate.put(date, 0);
        }

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            LocalDate date = quizSetQuiz.getUpdatedAt().toLocalDate();

            if (!date.isBefore(lastMonthStart) && !date.isAfter(lastMonthEnd)) {
                lastMonthTotalQuizCountDateMap.put(date, lastMonthTotalQuizCountDateMap.getOrDefault(date, 0) + 1);
            }

            if (!date.isBefore(startOfDate) && !date.isAfter(endOfDate)) {
                totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + 1);

                if (!Objects.isNull(quizSetQuiz.getIsAnswer()) && quizSetQuiz.getIsAnswer()) {
                    correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + 1);
                }
            }

        }

        LocalDateTime startOfDay = startDate.atStartOfDay();
        LocalDateTime endOfDay = endOfDate.atTime(LocalTime.MAX);

        List<RandomQuizRecord> randomQuizRecords = randomQuizRecordRepository.findAllByMemberIdAndCreatedAtBetween(memberId, startOfDay, endOfDay);
        for (RandomQuizRecord randomQuizRecord : randomQuizRecords) {
            LocalDate date = randomQuizRecord.getUpdatedAt().toLocalDate();

            if (!date.isBefore(startOfDate) && !date.isAfter(endOfDate)) {
                int solvedQuizCount = randomQuizRecord.getSolvedQuizCount();
                int correctQuizCount = randomQuizRecord.getCorrectQuizCount();
                totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + solvedQuizCount);
                correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + correctQuizCount);
            }
        }

        List<CollectionRandomQuizRecord> collectionRandomQUizRecords = collectionRandomQuizRecordRepository.findAllByMemberIdAndCreatedAtBetween(memberId, startOfDay, endOfDay);
        for (CollectionRandomQuizRecord collectionRandomQUizRecord : collectionRandomQUizRecords) {
            LocalDate date = collectionRandomQUizRecord.getUpdatedAt().toLocalDate();

            if (!date.isBefore(startDate) && !date.isAfter(startDate.plusDays(7))) {
                int solvedQuizCount = collectionRandomQUizRecord.getSolvedQuizCount();
                int correctQuizCount = collectionRandomQUizRecord.getCorrectQuizCount();
                totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + solvedQuizCount);
                correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + correctQuizCount);
            }
        }

        Map<CollectionCategory, Integer> collectionFieldMap = new HashMap<>();
        List<CollectionQuizSet> collectionQuizSets = collectionQuizSetRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, startOfDay, endOfDay);

        for (CollectionQuizSet collectionQuizSet : collectionQuizSets) {
            LocalDate date = collectionQuizSet.getCreatedAt().toLocalDate();
            List<CollectionQuizSetCollectionQuiz> collectionQuizSetCollectionQuizzes = collectionQuizSet.getCollectionQuizSetCollectionQuizzes();
            int totalQuizCount = collectionQuizSetCollectionQuizzes.size();

            if (!date.isBefore(lastMonthStart) && !date.isAfter(lastMonthEnd)) {
                lastMonthTotalQuizCountDateMap.put(date, lastMonthTotalQuizCountDateMap.getOrDefault(date, 0) + totalQuizCount);
            }

            if (!date.isBefore(startOfDate) && !date.isAfter(endOfDate)) {
                totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + totalQuizCount);
                for (CollectionQuizSetCollectionQuiz collectionQuizSetCollectionQuiz : collectionQuizSetCollectionQuizzes) {
                    if (!Objects.isNull(collectionQuizSetCollectionQuiz.getIsAnswer()) && collectionQuizSetCollectionQuiz.getIsAnswer()) {
                        correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + 1);
                    }
                }
            }


            Collection collection = collectionQuizSet.getCollectionQuizSetCollectionQuizzes().getFirst().getCollectionQuiz().getCollection();
            collectionFieldMap.putIfAbsent(collection.getCollectionCategory(), totalQuizCount);
        }

        // 전월 날짜 범위의 퀴즈 개수 합산
        int lastMonthQuizCount = 0;
        for (int i = 0; i <= currentDate.getDayOfMonth() - lastMonthStart.getDayOfMonth(); i++) {
            LocalDate date = lastMonthStart.plusDays(i);
            lastMonthQuizCount += lastMonthTotalQuizCountDateMap.getOrDefault(date, 0);
        }

        // 이번 달 날짜 범위의 퀴즈 개수 합산
        int currentMonthQuizCount = 0;
        for (int i = 0; i <= currentDate.getDayOfMonth() - startOfDate.getDayOfMonth(); i++) {
            LocalDate date = startOfDate.plusDays(i);
            currentMonthQuizCount += totalQuizCountByDate.getOrDefault(date, 0);
        }

        // 퀴즈 개수 차이 계산
        int quizCountDifferenceFromLastMonth = currentMonthQuizCount - lastMonthQuizCount;

        List<GetQuizMonthlyAnalysisResponse.QuizAnswerRateAnalysisDto> quizzesDtos = new ArrayList<>();


        for (LocalDate date : totalQuizCountByDate.keySet()) {
            int totalQuizCount = totalQuizCountByDate.getOrDefault(date, 0);
            int correctAnswerCount = correctAnswerCountByDate.getOrDefault(date, 0);

            monthlyTotalQuizCount += totalQuizCount;
            monthlyTotalCorrectAnswerCount += correctAnswerCount;

            GetQuizMonthlyAnalysisResponse.QuizAnswerRateAnalysisDto quizzesDto = GetQuizMonthlyAnalysisResponse.QuizAnswerRateAnalysisDto.builder()
                    .date(date)
                    .totalQuizCount(totalQuizCount)
                    .correctAnswerCount(correctAnswerCount)
                    .build();

            quizzesDtos.add(quizzesDto);
        }

        double averageCorrectRate = (double) monthlyTotalCorrectAnswerCount / (double) monthlyTotalQuizCount * 100.0;

        return new GetQuizMonthlyAnalysisResponse(quizzesDtos, monthlyTotalQuizCount, monthlyTotalCorrectAnswerCount, averageCorrectRate, quizCountDifferenceFromLastMonth, collectionFieldMap);
    }
}

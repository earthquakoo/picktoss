package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizMonthlyAnalysisResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizWeeklyAnalysisResponse;
import com.picktoss.picktossserver.domain.quiz.entity.DailyQuizRecord;
import com.picktoss.picktossserver.domain.quiz.entity.DailyQuizRecordDetail;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordDetailRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
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
    private final DailyQuizRecordDetailRepository dailyQuizRecordDetailRepository;

    public GetQuizWeeklyAnalysisResponse findQuizWeeklyAnalysis(Long memberId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startOfDay = startDate.atStartOfDay();
        LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);

        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, startOfDay, endOfDay);
        List<DailyQuizRecordDetail> dailyQuizRecordDetails = dailyQuizRecordDetailRepository.findAllByMemberIdAndDate(memberId, startDate, endDate);

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

            totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + 1);
            if (!Objects.isNull(quizSetQuiz.getIsAnswer()) && quizSetQuiz.getIsAnswer()) {
                correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + 1);
            }
        }

        for (DailyQuizRecordDetail dailyQuizRecordDetail : dailyQuizRecordDetails) {
            DailyQuizRecord dailyQuizRecord = dailyQuizRecordDetail.getDailyQuizRecord();
            LocalDate date = dailyQuizRecord.getSolvedDate();

            totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + 1);
            if (!Objects.isNull(dailyQuizRecordDetail.getIsAnswer()) && dailyQuizRecordDetail.getIsAnswer()) {
                correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + 1);
            }
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

        return new GetQuizWeeklyAnalysisResponse(quizzesDtos, averageDailyQuizCount, averageCorrectRate, weeklyTotalQuizCount);
    }

    public GetQuizMonthlyAnalysisResponse findQuizMonthlyAnalysis(Long memberId, LocalDate startMonthDate) {
        YearMonth yearMonth = YearMonth.of(startMonthDate.getYear(), startMonthDate.getMonth());
        LocalDate startOfDate = yearMonth.atDay(1);
        LocalDate endOfDate = yearMonth.atEndOfMonth();

        LocalDate currentDate = LocalDate.now();
        LocalDate lastMonthStart = startMonthDate.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = startMonthDate.minusMonths(1).withDayOfMonth(currentDate.getDayOfMonth());

        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, lastMonthStart.atStartOfDay(), endOfDate.atTime(LocalTime.MAX));
        List<DailyQuizRecordDetail> dailyQuizRecordDetails = dailyQuizRecordDetailRepository.findAllByMemberIdAndDate(memberId, lastMonthStart, endOfDate);

        HashMap<LocalDate, Integer> correctAnswerCountByDate = new LinkedHashMap<>();
        HashMap<LocalDate, Integer> totalQuizCountByDate = new LinkedHashMap<>();
        HashMap<LocalDate, Integer> lastMonthTotalQuizCountDateMap = new LinkedHashMap<>();

        int monthlyTotalQuizCount = 0;
        int monthlyTotalCorrectAnswerCount = 0;

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

        for (DailyQuizRecordDetail dailyQuizRecordDetail : dailyQuizRecordDetails) {
            DailyQuizRecord dailyQuizRecord = dailyQuizRecordDetail.getDailyQuizRecord();
            LocalDate date = dailyQuizRecord.getSolvedDate();

            if (!date.isBefore(lastMonthStart) && !date.isAfter(lastMonthEnd)) {
                lastMonthTotalQuizCountDateMap.put(date, lastMonthTotalQuizCountDateMap.getOrDefault(date, 0) + 1);
            }

            if (!date.isBefore(startOfDate) && !date.isAfter(endOfDate)) {
                totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + 1);

                if (!Objects.isNull(dailyQuizRecordDetail.getIsAnswer()) && dailyQuizRecordDetail.getIsAnswer()) {
                    correctAnswerCountByDate.put(date, correctAnswerCountByDate.getOrDefault(date, 0) + 1);
                }
            }
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

        return new GetQuizMonthlyAnalysisResponse(quizzesDtos, monthlyTotalQuizCount, monthlyTotalCorrectAnswerCount, averageCorrectRate, quizCountDifferenceFromLastMonth);
    }
}

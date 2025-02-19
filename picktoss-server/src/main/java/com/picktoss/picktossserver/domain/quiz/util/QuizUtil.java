package com.picktoss.picktossserver.domain.quiz.util;

import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class QuizUtil {

    public int checkCurrentConsecutiveSolvedQuizSet(List<QuizSet> quizSets) {
        if (quizSets.isEmpty()) {
            return 0;
        }

        LocalDate firstQuizSetDate = quizSets.getFirst().getCreatedAt().toLocalDate();
        // 가장 최근 quizSet이 오늘이나 어제가 아니라면 return 0
        if (!firstQuizSetDate.equals(LocalDate.now()) && !firstQuizSetDate.equals(LocalDate.now().minusDays(1))) {
            return 0;
        }

        int currentConsecutiveDays = 0;
        LocalDate previousDate = null;

        for (QuizSet quizSet : quizSets) {
            LocalDate quizDate = quizSet.getCreatedAt().toLocalDate();

            if (previousDate == null) {
                currentConsecutiveDays += 1;
            } else if (previousDate.minusDays(1).equals(quizDate)) {
                currentConsecutiveDays += 1;
            } else {
                break;
            }
            previousDate = quizDate;
        }
        return currentConsecutiveDays;
    }

    public int checkMaxConsecutiveSolvedQuizSet(List<QuizSet> quizSets) {
        if (quizSets.isEmpty()) {
            return 0;
        }

        int currentConsecutiveDays = 0;
        int maxConsecutiveDays = 0;
        LocalDate previousDate = null;

        for (QuizSet quizSet : quizSets) {
            LocalDate quizDate = quizSet.getCreatedAt().toLocalDate();

            if (previousDate == null || previousDate.minusDays(1).equals(quizDate)) {
                // 연속된 날짜일 경우 currentConsecutiveDays 증가
                currentConsecutiveDays++;
            } else {
                // 연속되지 않으면 최대값을 갱신하고 현재 연속일 초기화
                maxConsecutiveDays = Math.max(maxConsecutiveDays, currentConsecutiveDays);
                currentConsecutiveDays = 1;  // 새로 시작된 연속일
            }

            previousDate = quizDate;  // 다음 비교를 위해 이전 날짜 업데이트
        }
        return Math.max(maxConsecutiveDays, currentConsecutiveDays);
    }

    public boolean checkTodayQuizSetSolvedStatus(List<QuizSet> quizSets) {
        LocalDate today = LocalDate.now();

        if (quizSets.isEmpty()) {
            return false;
        }

        for (QuizSet quizSet : quizSets) {
            if (quizSet.getCreatedAt().toLocalDate().equals(today)) {
                if (quizSet.isSolved()) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean checkConsecutiveUnsolvedQuizSetsOverFourDays(List<QuizSet> quizSets) {
        LocalDate today = LocalDate.now();
        LocalDate fourDaysAgo = today.minusDays(4);
        int consecutiveUnsolvedDays = 0; // 연속적으로 풀지 않은 날 수

        if (quizSets.isEmpty()) {
            return false;
        }

        // quizSets를 최신 순으로 정렬
        Collections.sort(quizSets, (q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt()));

        // 가장 최근에 풀지 않은 quizSet부터 체크
        for (QuizSet quizSet : quizSets) {
            LocalDate quizSetDate = quizSet.getCreatedAt().toLocalDate();

            if (quizSetDate.isAfter(fourDaysAgo)) { // 4일 이내의 QuizSet에 대해서만 체크
                if (!quizSet.isSolved()) { // 풀지 않았다면 연속 미풀이 날짜 증가
                    consecutiveUnsolvedDays++;
                    if (consecutiveUnsolvedDays >= 4) {
                        return true; // 연속적으로 4일 이상 풀지 않았다면 false 반환
                    }
                } else {
                    consecutiveUnsolvedDays = 0; // 풀었다면 연속 미풀이 날짜 초기화
                }
            }
        }

        return false;
    }
}

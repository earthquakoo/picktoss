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

            if (previousDate == null || previousDate.minusDays(1).equals(quizDate)) {
                currentConsecutiveDays += 1;
            } else if (!previousDate.equals(quizDate)) {
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
                currentConsecutiveDays += 1;
            } else if (!previousDate.equals(quizDate)) {
                maxConsecutiveDays = Math.max(maxConsecutiveDays, currentConsecutiveDays);
                currentConsecutiveDays = 1;
            }

            previousDate = quizDate;
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

        Collections.sort(quizSets, (q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt()));

        for (QuizSet quizSet : quizSets) {
            LocalDate quizSetDate = quizSet.getCreatedAt().toLocalDate();

            if (quizSetDate.isAfter(fourDaysAgo)) {
                if (!quizSet.isSolved()) {
                    consecutiveUnsolvedDays++;
                    if (consecutiveUnsolvedDays >= 4) {
                        return true;
                    }
                } else {
                    consecutiveUnsolvedDays = 0;
                }
            }
        }

        return false;
    }
}

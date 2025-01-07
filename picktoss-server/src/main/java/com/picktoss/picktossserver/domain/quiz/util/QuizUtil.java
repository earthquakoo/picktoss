package com.picktoss.picktossserver.domain.quiz.util;

import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
                currentConsecutiveDays++;
            } else if (previousDate.minusDays(1).equals(quizDate)) {
                currentConsecutiveDays++;
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

    public static String createQuizSetId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

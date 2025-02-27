package com.picktoss.picktossserver.domain.notification.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationUtil {

    // 현재 요일 이후 가장 빠른 반복 요일을 찾음
    public DayOfWeek findNextDay(List<DayOfWeek> repeatDays, DayOfWeek currentDay) {
        // 요일을 정렬 (월요일부터 일요일 순서)
        repeatDays.sort(Comparator.comparingInt(DayOfWeek::getValue));

        for (DayOfWeek day : repeatDays) {
            if (day.getValue() > currentDay.getValue()) {
                return day; // 현재 요일보다 나중 요일 반환
            }
        }
        // 반복문을 다 돌았지만 조건에 맞는 요일이 없으면 첫 번째 요일 반환
        return repeatDays.getFirst();
    }

    // 다음 알림 날짜 계산
    public LocalDateTime calculateNextNotificationTime(LocalDateTime notificationTime, DayOfWeek nextDay) {
        // nextDay가 현재 요일과 같다면 정확히 1주 후로 설정
        if (notificationTime.getDayOfWeek() == nextDay) {
            return notificationTime.plusWeeks(1);
        }
        // 그렇지 않다면, 다음 해당 요일로 이동
        return notificationTime.with(TemporalAdjusters.next(nextDay));
    }

    public Instant toInstant(LocalDateTime startTime) {
        return startTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public List<String> dayOfWeeksToString(List<DayOfWeek> repeatDays) {
        return repeatDays.stream()
                .map(DayOfWeek::name)
                .collect(Collectors.toList());
    }

    public List<DayOfWeek> stringsToDayOfWeeks(List<String> dayStrings) {
        return dayStrings.stream()
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toList());
    }

    public String createNotificationKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

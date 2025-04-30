package com.picktoss.picktossserver.domain.notification.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationUtil {

    public DayOfWeek findNextDay(List<DayOfWeek> repeatDays, DayOfWeek currentDay) {
        repeatDays.sort(Comparator.comparingInt(DayOfWeek::getValue));
        System.out.println("repeatDays = " + repeatDays);
        for (DayOfWeek day : repeatDays) {
            if (day.getValue() > currentDay.getValue()) {
                return day;
            }
        }
        return repeatDays.getFirst();
    }

    public LocalDateTime calculateNextNotificationTime(LocalDateTime notificationTime, DayOfWeek nextDay) {
        LocalDate nextDate = notificationTime.toLocalDate().with(TemporalAdjusters.next(nextDay));
        return LocalDateTime.of(nextDate, notificationTime.toLocalTime());
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

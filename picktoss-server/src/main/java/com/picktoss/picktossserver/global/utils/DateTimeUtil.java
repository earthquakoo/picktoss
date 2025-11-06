package com.picktoss.picktossserver.global.utils;

import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class DateTimeUtil {

    public LocalDate convertToMemberLocalDate(LocalDateTime utcDateTime, ZoneId memberZoneId) {
        if (utcDateTime == null || memberZoneId == null) {
            return null;
        }
        return utcDateTime.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(memberZoneId)
                .toLocalDate();
    }

    public LocalDateTime convertToMemberLocalDateTime(LocalDateTime utcDateTime, ZoneId memberZoneId) {
        if (utcDateTime == null || memberZoneId == null) {
            return null;
        }

        return utcDateTime.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(memberZoneId)
                .toLocalDateTime();
    }
}

package com.picktoss.picktossserver.domain.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.picktoss.picktossserver.global.enums.notification.NotificationTarget;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
public class UpdateNotificationRequest {
    private NotificationType notificationType;
    private NotificationTarget notificationTarget;
    @Schema(type = "string", example = "19:30:00", description = "알림 발송 시간 (HH:mm:ss)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime notificationTime;
    private String title;
    private String content;
    private String memo;
    private String language;
    private List<DayOfWeek> repeatDays;
    private Boolean isActive;
}

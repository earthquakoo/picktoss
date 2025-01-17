package com.picktoss.picktossserver.domain.admin.controller.request;

import com.picktoss.picktossserver.global.enums.notification.NotificationTarget;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CreateNotificationRequest {
    private String title;
    private String content;
    private String memo;
    private NotificationType notificationType;
    private NotificationTarget notificationTarget;
    private Boolean isActive;
    private LocalDateTime notificationTime;
    private List<DayOfWeek> repeatDays;
}

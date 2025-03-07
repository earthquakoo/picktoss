package com.picktoss.picktossserver.domain.admin.dto.request;

import com.picktoss.picktossserver.global.enums.notification.NotificationTarget;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class UpdateNotificationRequest {
    private NotificationType notificationType;
    private NotificationTarget notificationTarget;
    private LocalDateTime notificationTime;
    private String title;
    private String content;
    private String memo;
    private List<DayOfWeek> repeatDays;
    private Boolean isActive;
}

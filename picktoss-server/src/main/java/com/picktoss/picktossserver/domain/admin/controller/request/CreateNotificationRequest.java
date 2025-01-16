package com.picktoss.picktossserver.domain.admin.controller.request;

import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.Getter;

@Getter
public class CreateNotificationRequest {
    private String title;
    private String content;
    private String memo;
    private NotificationType notificationType;
}

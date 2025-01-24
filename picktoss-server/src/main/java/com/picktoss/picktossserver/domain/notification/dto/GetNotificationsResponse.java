package com.picktoss.picktossserver.domain.notification.dto;

import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetNotificationsResponse {

    private List<GetNotificationsDto> notifications;

    @Getter
    @Builder
    public static class GetNotificationsDto {
        private String notificationKey;
        private String title;
        private String content;
        private NotificationType notificationType;
        private LocalDateTime receivedTime;
    }
}

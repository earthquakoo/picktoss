package com.picktoss.picktossserver.domain.admin.dto.response;

import com.picktoss.picktossserver.global.enums.notification.NotificationTarget;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetNotificationsForAdminResponse {

    private List<GetNotificationsForAdminDto> notifications;
    private long totalNotifications;
    private int totalPages;


    @Getter
    @Builder
    public static class GetNotificationsForAdminDto {
        private Long id;
        private String title;
        private String content;
        private String memo;
        private NotificationType notificationType;
        private List<String> repeatDays;
        private Boolean isActive;
        private LocalDateTime notificationTime;
        private NotificationTarget notificationTarget;
    }
}

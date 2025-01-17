package com.picktoss.picktossserver.domain.admin.controller.response;

import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetNotificationsResponse {

    private List<GetNotificationsDto> notifications;

    @Getter
    @Builder
    public static class GetNotificationsDto {
        private Long id;
        private String title;
        private String content;
        private String memo;
        private NotificationType notificationType;
        private List<String> repeatDays;
    }
}

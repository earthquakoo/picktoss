package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.domain.admin.controller.response.GetNotificationsResponse;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.global.enums.notification.NotificationSearchOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationSearchService {

    private final NotificationRepository notificationRepository;

    public GetNotificationsResponse findAllNotification() {
        List<Notification> notifications = notificationRepository.findAll();

        List<GetNotificationsResponse.GetNotificationsDto> notificationDtos = new ArrayList<>();

        for (Notification notification : notifications) {
            GetNotificationsResponse.GetNotificationsDto notificationDto = GetNotificationsResponse.GetNotificationsDto.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .memo(notification.getMemo())
                    .notificationType(notification.getNotificationType())
                    .repeatDays(notification.getRepeatDays())
                    .notificationTarget(notification.getNotificationTarget())
                    .notificationTime(notification.getNotificationTime())
                    .build();

            notificationDtos.add(notificationDto);
        }

        return new GetNotificationsResponse(notificationDtos);
    }

    public GetNotificationsResponse searchNotifications(String keyword, NotificationSearchOption notificationSearchOption) {
        List<Notification> notifications;

        if (notificationSearchOption == NotificationSearchOption.TITLE_AND_CONTENT && keyword != null) {
            notifications = notificationRepository.findAllByTitleOrContent(keyword);
        } else if (notificationSearchOption == NotificationSearchOption.TITLE && keyword != null) {
            notifications = notificationRepository.findAllByTitle(keyword);
        } else if (notificationSearchOption == NotificationSearchOption.CONTENT && keyword != null) {
            notifications = notificationRepository.findAllByTitleOrContent(keyword);
        } else {
            notifications = notificationRepository.findAll();
        }

        List<GetNotificationsResponse.GetNotificationsDto> notificationDtos = new ArrayList<>();

        for (Notification notification : notifications) {
            GetNotificationsResponse.GetNotificationsDto notificationDto = GetNotificationsResponse.GetNotificationsDto.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .memo(notification.getMemo())
                    .notificationType(notification.getNotificationType())
                    .repeatDays(notification.getRepeatDays())
                    .notificationTarget(notification.getNotificationTarget())
                    .notificationTime(notification.getNotificationTime())
                    .build();

            notificationDtos.add(notificationDto);
        }

        return new GetNotificationsResponse(notificationDtos);
    }
}

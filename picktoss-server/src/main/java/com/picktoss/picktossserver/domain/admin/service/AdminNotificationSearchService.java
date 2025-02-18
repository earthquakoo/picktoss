package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.admin.dto.response.GetNotificationsForAdminResponse;
import com.picktoss.picktossserver.domain.admin.dto.response.GetSingleNotificationForAdminResponse;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.global.enums.notification.NotificationSearchOption;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public GetNotificationsForAdminResponse findAllNotification(int page) {
        Pageable pageable = PageRequest.of(page, 15);
        Page<Notification> notifications = notificationRepository.findAll(pageable);
        long totalNotifications = notifications.getTotalElements();
        int totalPages = notifications.getTotalPages();

        List<GetNotificationsForAdminResponse.GetNotificationsForAdminDto> notificationDtos = new ArrayList<>();

        for (Notification notification : notifications) {
            GetNotificationsForAdminResponse.GetNotificationsForAdminDto notificationDto = GetNotificationsForAdminResponse.GetNotificationsForAdminDto.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .memo(notification.getMemo())
                    .notificationType(notification.getNotificationType())
                    .repeatDays(notification.getRepeatDays())
                    .notificationTarget(notification.getNotificationTarget())
                    .notificationTime(notification.getNotificationTime())
                    .isActive(notification.getIsActive())
                    .build();

            notificationDtos.add(notificationDto);
        }

        return new GetNotificationsForAdminResponse(notificationDtos, totalNotifications, totalPages);
    }

    public GetNotificationsForAdminResponse searchNotifications(
            int page,
            String keyword,
            NotificationSearchOption notificationSearchOption,
            NotificationType notificationType,
            Boolean isActive) {

        Page<Notification> notifications = filterNotificationSearch(page, keyword, notificationSearchOption, notificationType, isActive);
        long totalNotifications = notifications.getTotalElements();
        int totalPages = notifications.getTotalPages();

        List<GetNotificationsForAdminResponse.GetNotificationsForAdminDto> notificationDtos = new ArrayList<>();

        for (Notification notification : notifications) {
            GetNotificationsForAdminResponse.GetNotificationsForAdminDto notificationDto = GetNotificationsForAdminResponse.GetNotificationsForAdminDto.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .memo(notification.getMemo())
                    .notificationType(notification.getNotificationType())
                    .repeatDays(notification.getRepeatDays())
                    .notificationTarget(notification.getNotificationTarget())
                    .notificationTime(notification.getNotificationTime())
                    .isActive(notification.getIsActive())
                    .build();

            notificationDtos.add(notificationDto);
        }

        return new GetNotificationsForAdminResponse(notificationDtos, totalNotifications, totalPages);
    }

    public GetSingleNotificationForAdminResponse findSingleNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        return GetSingleNotificationForAdminResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .memo(notification.getMemo())
                .notificationType(notification.getNotificationType())
                .repeatDays(notification.getRepeatDays())
                .notificationTarget(notification.getNotificationTarget())
                .notificationTime(notification.getNotificationTime())
                .isActive(notification.getIsActive())
                .build();
    }

    private Page<Notification> filterNotificationSearch(
            int page,
            String keyword,
            NotificationSearchOption searchOption,
            NotificationType notificationType,
            Boolean isActive
            ) {
        Pageable pageable = PageRequest.of(page, 15);

        if (keyword == null || keyword.isBlank()) {
            return notificationRepository.findAllByNotificationTypeOrIsActive(notificationType, isActive, pageable);
        }

        return switch (searchOption) {
            case TITLE_AND_CONTENT -> notificationRepository.findAllByTitleOrContent(keyword, notificationType, isActive, pageable);
            case TITLE -> notificationRepository.findAllByTitle(keyword, notificationType, isActive, pageable);
            case CONTENT -> notificationRepository.findAllByContent(keyword, notificationType, isActive, pageable);
        };
    }
}
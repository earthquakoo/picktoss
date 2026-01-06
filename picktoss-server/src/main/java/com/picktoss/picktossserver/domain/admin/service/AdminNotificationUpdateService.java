package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.domain.notification.util.NotificationScheduler;
import com.picktoss.picktossserver.domain.notification.util.NotificationUtil;
import com.picktoss.picktossserver.global.enums.notification.NotificationTarget;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationUpdateService {

    private final NotificationRepository notificationRepository;
    private final NotificationUtil notificationUtil;
    private final NotificationScheduler notificationScheduler;

    @Transactional
    public void updateNotification(
            Long notificationId,
            String title,
            String content,
            String memo,
            NotificationType notificationType,
            NotificationTarget notificationTarget,
            Boolean isActive,
            LocalTime notificationTime,
            String language,
            List<DayOfWeek> dayOfWeeks
    ) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        List<String> repeatDays = notificationUtil.dayOfWeeksToString(dayOfWeeks);

        notificationScheduler.cancelNotificationSchedule(notification.getId());

        notification.updateNotificationInfo(
                title,
                content,
                memo,
                notificationType,
                notificationTarget,
                isActive,
                notificationTime,
                language,
                repeatDays
        );

        if (Boolean.TRUE.equals(isActive)) {
            notification.updateNotificationStatusPending();

            notificationScheduler.registerNewNotification(notification);
        }
    }
}

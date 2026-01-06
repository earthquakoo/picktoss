package com.picktoss.picktossserver.domain.admin.service;

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
public class AdminNotificationCreateService {

    private final NotificationRepository notificationRepository;
    private final NotificationUtil notificationUtil;
    private final NotificationScheduler notificationScheduler;

    @Transactional
    public void createNotification(
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

        List<String> repeatDays = notificationUtil.dayOfWeeksToString(dayOfWeeks);

        Notification notification = Notification.createNotification(
                title, content, memo, notificationType, notificationTarget,
                isActive, notificationTime, language, repeatDays
        );

        notificationRepository.save(notification);

        if (Boolean.TRUE.equals(isActive)) {
            notificationScheduler.registerNewNotification(notification);
        }
    }
}

package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.domain.notification.util.NotificationSchedulerUtil;
import com.picktoss.picktossserver.domain.notification.util.NotificationUtil;
import com.picktoss.picktossserver.global.enums.notification.NotificationTarget;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationUpdateService {

    private final NotificationRepository notificationRepository;
    private final NotificationUtil notificationUtil;
    private final NotificationSchedulerUtil notificationSchedulerUtil;

    @Transactional
    public void updateNotification(Long notificationId, String title, String content, String memo, NotificationType notificationType, NotificationTarget notificationTarget, Boolean isActive, LocalDateTime notificationTime, List<DayOfWeek> dayOfWeeks) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        List<String> repeatDays = notificationUtil.dayOfWeeksToString(dayOfWeeks);

        if (!isActive) {
            notificationSchedulerUtil.cancelScheduleTask(notification.getId());
            notification.updateNotificationInfo(title, content, memo, notificationType, notificationTarget, isActive, notificationTime, repeatDays);
            return;
        }

        // notificationTime이 현재보다 이전이라면
        if (notificationTime.isBefore(LocalDateTime.now())) {
            if (repeatDays == null || repeatDays.isEmpty()) {
                throw new CustomException(ErrorInfo.INVALID_NOTIFICATION_TIME);
            } else {
                notificationSchedulerUtil.cancelScheduleTask(notification.getId());
                DayOfWeek nextDay = notificationUtil.findNextDay(dayOfWeeks, notification.getNotificationTime().getDayOfWeek());
                LocalDateTime nextNotificationTime = notificationUtil.calculateNextNotificationTime(notificationTime, nextDay);
                notificationSchedulerUtil.scheduleTask(notification , nextNotificationTime);
                notification.updateNotificationInfo(title, content, memo, notificationType, notificationTarget, isActive, nextNotificationTime, repeatDays);
                notification.updateNotificationStatusPending();
            }
        } else {
            notificationSchedulerUtil.cancelScheduleTask(notification.getId());
            notificationSchedulerUtil.scheduleTask(notification , notificationTime);
            notification.updateNotificationInfo(title, content, memo, notificationType, notificationTarget, isActive, notificationTime, repeatDays);
            notification.updateNotificationStatusPending();
        }
    }
}

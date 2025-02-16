package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.domain.admin.util.AdminNotificationUtil;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.domain.notification.util.NotificationSchedulerUtil;
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
public class AdminNotificationCreateService {

    private final NotificationRepository notificationRepository;
    private final AdminNotificationUtil adminNotificationUtil;
    private final NotificationSchedulerUtil notificationSchedulerUtil;

    @Transactional
    public void createNotification(String title, String content, String memo, NotificationType notificationType, NotificationTarget notificationTarget, Boolean isActive, LocalDateTime notificationTime, List<DayOfWeek> dayOfWeeks, Long memberId) {
        List<String> repeatDays = adminNotificationUtil.dayOfWeeksToString(dayOfWeeks);

        String notificationKey = adminNotificationUtil.createNotificationKey();

        Notification notification = Notification.createNotification(title, content, memo, notificationKey, notificationType, notificationTarget, isActive, notificationTime, repeatDays);
        notificationRepository.save(notification);

        notificationSchedulerUtil.scheduleNotification(notification , notificationTime);
    }
}

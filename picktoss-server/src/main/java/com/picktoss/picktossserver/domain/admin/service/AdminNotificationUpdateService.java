package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.admin.util.AdminNotificationUtil;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
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
    private final AdminNotificationUtil adminNotificationUtil;


    @Transactional
    public void updateNotification(Long notificationId, String title, String content, String memo, NotificationType notificationType, NotificationTarget notificationTarget, Boolean isActive, LocalDateTime notificationTime, List<DayOfWeek> dayOfWeeks) {
        List<String> repeatDays = adminNotificationUtil.dayOfWeeksToString(dayOfWeeks);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        notification.updateNotificationInfo(title, content, memo, notificationType, notificationTarget, isActive, notificationTime, repeatDays);
    }
}

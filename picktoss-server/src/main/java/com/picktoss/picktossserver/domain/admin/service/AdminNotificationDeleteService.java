package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.domain.notification.util.NotificationSchedulerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationDeleteService {

    private final NotificationRepository notificationRepository;
    private final NotificationSchedulerUtil notificationSchedulerUtil;

    @Transactional
    public void deleteNotifications(List<Long> notificationIds) {
        List<Notification> notifications = notificationRepository.findAllByNotificationIds(notificationIds);
        for (Notification notification : notifications) {
            notificationSchedulerUtil.cancelScheduleTask(notification.getId());
        }
        notificationRepository.deleteAll(notifications);
    }
}

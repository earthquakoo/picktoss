package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
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

    @Transactional
    public void deleteNotifications(List<Long> notificationIds) {
        List<Notification> notifications = notificationRepository.findAllByNotificationIds(notificationIds);
        notificationRepository.deleteAll(notifications);
    }
}

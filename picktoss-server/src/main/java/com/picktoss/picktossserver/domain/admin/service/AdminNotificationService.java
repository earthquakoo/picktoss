package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(String title, String content, String memo, NotificationType notificationType) {
        Notification notification = Notification.createNotification(title, content, memo, notificationType);
        notificationRepository.save(notification);
    }

    public void findAllNotification() {

    }
}

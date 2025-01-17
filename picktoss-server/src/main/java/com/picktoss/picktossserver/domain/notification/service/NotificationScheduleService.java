package com.picktoss.picktossserver.domain.notification.service;

import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.global.enums.notification.NotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduleService {

    private final TaskScheduler taskScheduler;

    private final NotificationRepository notificationRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initNotificationSchedule() {
        List<Notification> notifications = notificationRepository.findAllByNotificationStatus(NotificationStatus.PENDING);

        for (Notification notification : notifications) {

        }
    }

    @Transactional
    public void createNotificationSchedule() {

    }
}

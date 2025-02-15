package com.picktoss.picktossserver.domain.admin.service;

import com.google.firebase.messaging.*;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.domain.admin.util.AdminNotificationUtil;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.global.enums.notification.NotificationTarget;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationTestService {


    private final RedisUtil redisUtil;
    private final TaskScheduler taskScheduler;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final AdminNotificationUtil adminNotificationUtil;



    @Transactional
    public void createNotificationTest(String title, String content, String memo, NotificationType notificationType, NotificationTarget notificationTarget, Boolean isActive, LocalDateTime notificationTime, List<DayOfWeek> dayOfWeeks, Long memberId) {
        List<String> repeatDays = adminNotificationUtil.dayOfWeeksToString(dayOfWeeks);

        String notificationKey = adminNotificationUtil.createNotificationKey();

        Notification notification = Notification.createNotification(title, content, memo, notificationKey, notificationType, notificationTarget, isActive, notificationTime, repeatDays);
        notificationRepository.save(notification);

        scheduleNextNotificationTest(notification , notificationTime, memberId);
    }


    @Transactional
    private void updateNotificationStatusCompleteBySendPushNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        notification.updateNotificationStatusComplete();
        notificationRepository.save(notification);
    }

    @Transactional
    private void updateNotificationStatusPendingBySendPushNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        notification.updateNotificationStatusPending();
        notificationRepository.save(notification);
    }

    @Transactional
    private void updateNotificationKeyBySendPushNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        String notificationKey = adminNotificationUtil.createNotificationKey();

        notification.updateNotificationKey(notificationKey);
        notificationRepository.save(notification);
    }

    // 다음 알림 예약
    private void scheduleNextNotificationTest(Notification notification, LocalDateTime baseTime, Long memberId) {
        List<DayOfWeek> repeatDays = adminNotificationUtil.stringsToDayOfWeeks(notification.getRepeatDays());

        if (repeatDays == null || repeatDays.isEmpty()) {
            // 단일 알림 스케줄
            taskScheduler.schedule(() -> sendAndScheduleNextNotificationTest(notification, memberId), adminNotificationUtil.toInstant(baseTime));
        } else {
            // 반복 조건 기반 첫 알림 스케줄
            DayOfWeek nextDay = adminNotificationUtil.findNextDay(repeatDays, baseTime.getDayOfWeek());
            LocalDateTime nextNotificationTime = adminNotificationUtil.calculateNextNotificationTime(baseTime, nextDay);
            taskScheduler.schedule(() -> sendAndScheduleNextNotificationTest(notification, memberId), adminNotificationUtil.toInstant(nextNotificationTime));
            updateNotificationStatusPendingBySendPushNotification(notification.getId());
            updateNotificationKeyBySendPushNotification(notification.getId());
        }
    }

    // 알림 발송하고 다음 알림이 있다면 예약
    private void sendAndScheduleNextNotificationTest(Notification notification, Long memberId) {
        // 알림 발송
        sendNotificationTest(notification.getTitle(), notification.getContent(), notification.getId(), memberId).run();

        List<DayOfWeek> repeatDays = adminNotificationUtil.stringsToDayOfWeeks(notification.getRepeatDays());
        if (repeatDays != null && !repeatDays.isEmpty()) {
            // 다음 반복 요일 계산
            DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
            DayOfWeek nextDay = adminNotificationUtil.findNextDay(repeatDays, currentDay);
            LocalDateTime nextNotificationTime = adminNotificationUtil.calculateNextNotificationTime(LocalDateTime.now(), nextDay);

            // 다음 알림 예약
            scheduleNextNotificationTest(notification, nextNotificationTime, memberId);
        }
    }

    private Runnable sendNotificationTest(String title, String body, Long notificationId, Long memberId) {
        return () -> {
            Optional<String> optionalToken = redisUtil.getData(RedisConstant.REDIS_FCM_PREFIX, memberId.toString(), String.class);
            if (optionalToken.isEmpty()) {
                throw new CustomException(ErrorInfo.MEMBER_NOT_FOUND);
            }
            String fcmToken = optionalToken.get();

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                    )
                    .setAndroidConfig(AndroidConfig.builder()
                            .setNotification(
                                    AndroidNotification.builder()
                                            .setTitle(title)
                                            .setBody(body)
                                            .setClickAction("push_click")
                                            .build())
                            .build()
                    )
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setAlert(ApsAlert.builder()
                                            .setTitle(title)
                                            .setBody(body)
                                            .build())
                                    .setSound("default")
                                    .setCategory("push_click")
                                    .build())
                            .build()
                    )
                    .build();
            try {
                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println("FCM Send Message = " + response);
            } catch (FirebaseMessagingException e) {
                System.out.println("FCM Exception = " + e.getMessage());
            }
            updateNotificationStatusCompleteBySendPushNotification(notificationId);
        };
    }
}

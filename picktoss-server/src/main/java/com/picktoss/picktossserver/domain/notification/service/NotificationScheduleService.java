package com.picktoss.picktossserver.domain.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.domain.notification.util.NotificationUtil;
import com.picktoss.picktossserver.global.enums.notification.NotificationStatus;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduleService {

    private final TaskScheduler taskScheduler;

    private final RedisUtil redisUtil;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final NotificationUtil notificationUtil;

    @EventListener(ApplicationReadyEvent.class)
    public void initNotificationSchedule() {
        List<Notification> notifications = notificationRepository.findAllByNotificationStatusAndIsActiveTrue(NotificationStatus.PENDING);

        for (Notification notification : notifications) {
            if (!notification.getNotificationTime().isBefore(LocalDateTime.now())) {
                scheduleNotification(notification, notification.getNotificationTime());
            }
        }
    }

    private void scheduleNotification(Notification notification, LocalDateTime notificationTime) {
        List<DayOfWeek> repeatDays = notificationUtil.stringsToDayOfWeeks(notification.getRepeatDays());

        if (repeatDays == null || repeatDays.isEmpty()) {
            // 단일 알림 스케줄
            scheduleTask(notification, notificationTime);
        } else {
            // 반복 조건 기반 알림 스케줄
            scheduleTask(notification, notificationTime);
            updateNotificationStatusPendingBySendPushNotification(notification.getId());
            updateNotificationKeyBySendPushNotification(notification.getId());
        }
        createNotificationForRedis(notification.getNotificationKey(), notification.getTitle(), notification.getContent(), notification.getNotificationTime(), notification.getNotificationType());
    }

    private void scheduleTask(Notification notification, LocalDateTime notificationTime) {
        taskScheduler.schedule(() -> handleNotification(notification), notificationUtil.toInstant(notificationTime));
    }

    private void handleNotification(Notification notification) {
        // 알림 발송
        sendNotification(notification.getTitle(), notification.getContent(), notification.getId(), notification.getNotificationKey()).run();

        List<DayOfWeek> repeatDays = notificationUtil.stringsToDayOfWeeks(notification.getRepeatDays());
        if (repeatDays != null && !repeatDays.isEmpty()) {
            // 다음 알림 예약
            DayOfWeek nextDay = notificationUtil.findNextDay(repeatDays, notification.getNotificationTime().getDayOfWeek());
            LocalDateTime nextNotificationTime = notificationUtil.calculateNextNotificationTime(notification.getNotificationTime(), nextDay);
            updateNotificationTimeBySendPushNotification(notification.getId(), nextNotificationTime);
            scheduleNotification(notification, nextNotificationTime);
        }
    }

//    private void scheduleNotification(Notification notification, LocalDateTime notificationTime) {
//        List<DayOfWeek> repeatDays = notificationUtil.stringsToDayOfWeeks(notification.getRepeatDays());
//
//        if (repeatDays == null || repeatDays.isEmpty()) {
//            // 단일 알림 스케줄
//            scheduleTask(notification, notificationTime);
//        } else {
//            // 반복 조건 기반 알림 스케줄
//            scheduleTask(notification, notificationTime);
//            updateNotificationStatusPendingBySendPushNotification(notification.getId());
//            updateNotificationKeyBySendPushNotification(notification.getId());
//        }
//        createNotificationForRedis(notification.getNotificationKey(), notification.getTitle(), notification.getContent(), notification.getNotificationTime(), notification.getNotificationType());
//    }
//
//    private void scheduleTask(Notification notification, LocalDateTime notificationTime) {
//        taskScheduler.schedule(() -> handleNotification(notification), notificationUtil.toInstant(notificationTime));
//    }
//
//    private void handleNotification(Notification notification) {
//        // 알림 발송
//        sendNotification(notification.getTitle(), notification.getContent(), notification.getId(), notification.getNotificationKey()).run();
//
//        List<DayOfWeek> repeatDays = notificationUtil.stringsToDayOfWeeks(notification.getRepeatDays());
//        if (repeatDays != null && !repeatDays.isEmpty()) {
//            // 다음 알림 예약
//            DayOfWeek nextDay = notificationUtil.findNextDay(repeatDays, notification.getNotificationTime().getDayOfWeek());
//            LocalDateTime nextNotificationTime = notificationUtil.calculateNextNotificationTime(notification.getNotificationTime(), nextDay);
//            updateNotificationTimeBySendPushNotification(notification.getId(), nextNotificationTime);
//            scheduleNotification(notification, nextNotificationTime);
//        }
//    }

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

        String notificationKey = notificationUtil.createNotificationKey();

        notification.updateNotificationKey(notificationKey);
        notificationRepository.save(notification);
    }

    @Transactional
    private void updateNotificationTimeBySendPushNotification(Long notificationId, LocalDateTime sendTime) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        notification.updateNotificationSendTime(sendTime);
        notificationRepository.save(notification);
    }

    private Runnable sendNotification(String title, String body, Long notificationId, String notificationKey) {
        return () -> {
            List<Member> members = memberRepository.findAllByIsQuizNotificationEnabledTrue();
            for (Member member : members) {
                addNotificationReceivedMemberData(member.getId(), notificationKey);

                Optional<String> optionalToken = redisUtil.getData(RedisConstant.REDIS_FCM_PREFIX, member.getId().toString(), String.class);
                if (optionalToken.isEmpty()) {
                    continue;
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
            }
            updateNotificationStatusCompleteBySendPushNotification(notificationId);
        };
    }

    private void createNotificationForRedis(String notificationKey, String title, String content, LocalDateTime notificationSendTime, NotificationType notificationType) {
        LocalDateTime createdAt = LocalDateTime.now();

        Map<String, Object> notificationData = Map.of(
                "title", title,
                "content", content,
                "notificationSendTime", notificationSendTime,
                "notificationType", notificationType.toString(),
                "createdAt", createdAt,
                "expiresAt", createdAt.plusWeeks(2)
        );
        redisUtil.setData(RedisConstant.REDIS_NOTIFICATION_PREFIX, notificationKey, notificationData, RedisConstant.REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS);
    }

    private void addNotificationReceivedMemberData(Long memberId, String notificationKey) {
        String memberIdKey = memberId.toString();

        Optional<Map> existingNotificationData = redisUtil.getData(RedisConstant.REDIS_NOTIFICATION_PREFIX, notificationKey, Map.class);
        if (existingNotificationData.isEmpty()) {
            return ;
        }

        Optional<Map> existingNotificationReceivedMemberData = redisUtil.getData(RedisConstant.REDIS_NOTIFICATION_RECEIVED_MEMBER_PREFIX, memberIdKey, Map.class);

        if (existingNotificationReceivedMemberData.isEmpty()) {
            createNotificationReceivedMemberData(memberIdKey, notificationKey);
        }

        if (existingNotificationReceivedMemberData.isPresent()) {
            Map notificationReceivedMemberData = existingNotificationReceivedMemberData.get();
            Object notificationKeysObject = notificationReceivedMemberData.get("notificationKeys");

            List<String> notificationKeys = new ObjectMapper().convertValue(notificationKeysObject, new TypeReference<List<String>>() {});
            notificationKeys.add(notificationKey);

            notificationReceivedMemberData.put("notificationKeys", notificationKeys);
            redisUtil.setData(RedisConstant.REDIS_NOTIFICATION_RECEIVED_MEMBER_PREFIX, memberIdKey, notificationReceivedMemberData);
        }
    }

    private void createNotificationReceivedMemberData(String memberIdKey, String notificationKey) {
        List<String> notificationKeys = new ArrayList<>();
        notificationKeys.add(notificationKey);

        Map<String, Object> notificationReceivedMemberData = Map.of(
                "notificationKeys", notificationKeys,
                "createdAt", LocalDateTime.now()
        );
        redisUtil.setData(RedisConstant.REDIS_NOTIFICATION_RECEIVED_MEMBER_PREFIX, memberIdKey, notificationReceivedMemberData);
    }
}

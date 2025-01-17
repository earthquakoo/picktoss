package com.picktoss.picktossserver.domain.admin.service;

import com.google.firebase.messaging.*;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.domain.member.entity.Member;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationService {

    private final RedisUtil redisUtil;
    private final TaskScheduler taskScheduler;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public void createNotification(String title, String content, String memo, NotificationType notificationType, NotificationTarget notificationTarget, Boolean isActive, LocalDateTime notificationTime, List<DayOfWeek> dayOfWeeks, Long memberId) {
        List<String> repeatDays = dayOfWeeksToString(dayOfWeeks);

        Notification notification = Notification.createNotification(title, content, memo, notificationType, notificationTarget, isActive, notificationTime, repeatDays);
        notificationRepository.save(notification);

        scheduleNextNotification(notification , notificationTime);
    }

    public void findAllNotification() {
        List<Notification> notifications = notificationRepository.findAll();
    }

    @Transactional
    public void deleteNotifications(List<Long> notificationIds) {
        List<Notification> notifications = notificationRepository.findAllByNotificationIds(notificationIds);
        notificationRepository.deleteAll(notifications);
    }

    @Transactional
    private void updateNotificationStatusBySendPushNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        notification.updateNotificationStatusComplete();
        notificationRepository.save(notification);
    }

    private Runnable sendNotification(String title, String body, Long notificationId) {
        return () -> {
            List<Member> members = memberRepository.findAllByIsQuizNotificationEnabledTrue();
            for (Member member : members) {
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
            updateNotificationStatusBySendPushNotification(notificationId);
        };
    }

    private Runnable sendPushNotification(String title, String body, Long memberId, Long notificationId) {
        return () -> {
            Optional<String> optionalToken = redisUtil.getData(RedisConstant.REDIS_FCM_PREFIX, memberId.toString(), String.class);
            if (optionalToken.isEmpty()) {
                throw new CustomException(ErrorInfo.FCM_TOKEN_NOT_FOUND);
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

            updateNotificationStatusBySendPushNotification(notificationId);
        };
    }

    // 다음 알림 예약
    private void scheduleNextNotification(Notification notification, LocalDateTime baseTime) {
        List<DayOfWeek> repeatDays = stringsToDayOfWeeks(notification.getRepeatDays());

        if (repeatDays == null || repeatDays.isEmpty()) {
            // 단일 알림 스케줄
            taskScheduler.schedule(() -> sendAndScheduleNext(notification), toInstant(baseTime));
        } else {
            // 반복 조건 기반 첫 알림 스케줄
            DayOfWeek nextDay = findNextDay(repeatDays, baseTime.getDayOfWeek());
            LocalDateTime nextNotificationTime = calculateNextNotificationTime(baseTime, nextDay);
            taskScheduler.schedule(() -> sendAndScheduleNext(notification), toInstant(nextNotificationTime));
        }
    }

    private void sendAndScheduleNext(Notification notification) {
        // 알림 전송
        sendNotification(notification.getTitle(), notification.getContent(), notification.getId()).run();

        List<DayOfWeek> repeatDays = stringsToDayOfWeeks(notification.getRepeatDays());
        if (repeatDays != null && !repeatDays.isEmpty()) {
            // 다음 반복 요일 계산
            DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
            DayOfWeek nextDay = findNextDay(repeatDays, currentDay);
            LocalDateTime nextNotificationTime = calculateNextNotificationTime(LocalDateTime.now(), nextDay);

            // 다음 알림 예약
            scheduleNextNotification(notification, nextNotificationTime);
        }
    }

    private DayOfWeek findNextDay(List<DayOfWeek> repeatDays, DayOfWeek currentDay) {
        // 현재 요일 이후 가장 빠른 반복 요일을 찾음
        return repeatDays.stream()
                .filter(day -> day.getValue() > currentDay.getValue())
                .findFirst()
                .orElse(repeatDays.get(0)); // 주의 시작으로 돌아감
    }

    private LocalDateTime calculateNextNotificationTime(LocalDateTime baseTime, DayOfWeek nextDay) {
        return baseTime.with(TemporalAdjusters.nextOrSame(nextDay));
    }

    private Instant toInstant(LocalDateTime startTime) {
        return startTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    private List<String> dayOfWeeksToString(List<DayOfWeek> repeatDays) {
        return repeatDays.stream()
                .map(DayOfWeek::name)
                .collect(Collectors.toList());
    }

    private List<DayOfWeek> stringsToDayOfWeeks(List<String> dayStrings) {
        return dayStrings.stream()
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toList());
    }
}

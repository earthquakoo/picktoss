package com.picktoss.picktossserver.domain.notification.util;

import com.google.firebase.messaging.*;
import com.picktoss.picktossserver.domain.fcm.entity.FcmToken;
import com.picktoss.picktossserver.domain.fcm.repository.FcmTokenRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.domain.quiz.entity.DailyQuizRecord;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordRepository;
import com.picktoss.picktossserver.global.enums.notification.NotificationStatus;
import com.picktoss.picktossserver.global.enums.notification.NotificationTarget;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationScheduler {

    private final TaskScheduler taskScheduler;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final NotificationUtil notificationUtil;
    private final DailyQuizRecordRepository dailyQuizRecordRepository;
    private final FcmTokenRepository fcmTokenRepository;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private static final String ZONE_SEOUL = "Asia/Seoul";
    private static final String LANG_KO = "ko";
    private static final String LANG_EN = "en";

    @EventListener(ApplicationReadyEvent.class)
    public void initNotificationSchedule() {
        List<Notification> notifications = notificationRepository.findAllByNotificationStatusAndIsActiveTrue(NotificationStatus.PENDING);
        List<String> distinctZoneIds = memberRepository.findAllDistinctZoneIds();

        for (Notification notification : notifications) {
            for (String zoneId : distinctZoneIds) {
                if (isLanguageMatch(notification.getLanguage(), zoneId)) {
                    scheduleTaskForZone(notification, zoneId);
                }
            }
        }
    }

    public void registerNewNotification(Notification notification) {
        List<String> distinctZoneIds = memberRepository.findAllDistinctZoneIds();

        for (String zoneId : distinctZoneIds) {
            if (isLanguageMatch(notification.getLanguage(), zoneId)) {
                scheduleTaskForZone(notification, zoneId);
            }
        }
    }

    public void cancelNotificationSchedule(Long notificationId) {
        String prefix = notificationId + "_";

        scheduledTasks.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(prefix)) {
                ScheduledFuture<?> future = entry.getValue();
                if (future != null) {
                    future.cancel(false);
                }
                return true;
            }
            return false;
        });

        log.info("Canceled all schedules for NotificationId: {}", notificationId);
    }


    private boolean isLanguageMatch(String language, String zoneId) {
        if (ZONE_SEOUL.equals(zoneId)) {
            return LANG_KO.equals(language);
        } else {
            return LANG_EN.equals(language);
        }
    }

    public void scheduleTaskForZone(Notification notification, String zoneId) {
        LocalTime targetLocalTime = notification.getNotificationTime();

        Instant nextExecutionTime = calculateNextExecutionTime(targetLocalTime, zoneId);

        ScheduledFuture<?> schedule = taskScheduler.schedule(
                () -> handleNotification(notification, zoneId),
                nextExecutionTime
        );

        scheduledTasks.put(generateTaskKey(notification.getId(), zoneId), schedule);
        log.info("[Schedule Init] NotificationId: {}, Zone: {}, NextTime: {}", notification.getId(), zoneId, nextExecutionTime);
    }

    private Instant calculateNextExecutionTime(LocalTime targetTime, String zoneIdStr) {
        ZoneId zoneId = ZoneId.of(zoneIdStr);
        ZonedDateTime nowInZone = ZonedDateTime.now(zoneId);

        ZonedDateTime targetDateTime = nowInZone.with(targetTime);

        if (targetDateTime.isBefore(nowInZone)) {
            targetDateTime = targetDateTime.plusDays(1);
        }

        return targetDateTime.toInstant();
    }

    private String generateTaskKey(Long notificationId, String zoneId) {
        return notificationId + "_" + zoneId;
    }

    private void handleNotification(Notification notification, String zoneId) {
        sendNotification(notification, zoneId).run();

        List<DayOfWeek> repeatDays = notificationUtil.stringsToDayOfWeeks(notification.getRepeatDays());

        if (repeatDays != null && !repeatDays.isEmpty()) {
            rescheduleForNextRepeatDay(notification, zoneId, repeatDays);
        } else {
            scheduledTasks.remove(generateTaskKey(notification.getId(), zoneId));
        }
    }

    private void rescheduleForNextRepeatDay(Notification notification, String zoneIdStr, List<DayOfWeek> repeatDays) {
        ZoneId zoneId = ZoneId.of(zoneIdStr);
        ZonedDateTime nowInZone = ZonedDateTime.now(zoneId);
        DayOfWeek currentDay = nowInZone.getDayOfWeek();

        DayOfWeek nextDay = notificationUtil.findNextDay(repeatDays, currentDay);

        int daysUntilNext = nextDay.getValue() - currentDay.getValue();
        if (daysUntilNext <= 0) {
            daysUntilNext += 7;
        }

        LocalTime targetTime = notification.getNotificationTime();

        ZonedDateTime nextRunZoned = nowInZone.plusDays(daysUntilNext).with(targetTime);

        if (nextRunZoned.toInstant().isBefore(Instant.now())) {
            nextRunZoned = nextRunZoned.plusWeeks(1);
        }

        ScheduledFuture<?> schedule = taskScheduler.schedule(
                () -> handleNotification(notification, zoneIdStr),
                nextRunZoned.toInstant()
        );
        scheduledTasks.put(generateTaskKey(notification.getId(), zoneIdStr), schedule);
        log.info("[Reschedule] NotificationId: {}, Zone: {}, NextTime: {}", notification.getId(), zoneIdStr, nextRunZoned);
    }

    private Runnable sendNotification(Notification notification, String targetZoneId) {
        return () -> {
            List<Member> members = memberRepository.findAllByZoneIdAndIsQuizNotificationEnabledTrue(targetZoneId);

            log.info("Sending notification to Zone: {}, Member Count: {}", targetZoneId, members.size());

            for (Member member : members) {
                if (!filterNotificationTarget(notification.getNotificationType(), notification.getNotificationTarget(), member, targetZoneId)) continue;

                List<FcmToken> fcmTokens = fcmTokenRepository.findAllByMemberId(member.getId());
                sendFcmToMember(notification, fcmTokens);
            }
        };
    }

    private void sendFcmToMember(Notification notification, List<FcmToken> fcmTokens) {
        for (FcmToken fcmToken : fcmTokens) {
            String token = fcmToken.getToken();
            Message message = Message.builder()
                    .setToken(token)
                    .putData("title", notification.getTitle())
                    .putData("content", notification.getContent())
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle(notification.getTitle())
                                    .setBody(notification.getContent())
                                    .build()
                    )
                    .setAndroidConfig(AndroidConfig.builder()
                            .setNotification(
                                    AndroidNotification.builder()
                                            .setClickAction("push_click")
                                            .build())
                            .build()
                    )
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setAlert(ApsAlert.builder()
                                            .build())
                                    .setSound("default")
                                    .setCategory("push_click")
                                    .build())
                            .build()
                    )
                    .build();
            try {
                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {
                log.error("FCM Send Error: {}", e.getMessage());
            }
        }
    }


    private boolean filterNotificationTarget(NotificationType notificationType, NotificationTarget notificationTarget, Member member, String zoneId) {
        if (notificationTarget == NotificationTarget.ALL) {
            return true;
        }

        if (notificationType == NotificationType.DAILY_QUIZ) {
            return notificationTargetByNotSolvedDailyQuiz(member, zoneId);
        }

        return true;
    }

    private boolean notificationTargetByNotSolvedDailyQuiz(Member member, String zoneIdStr) {
        LocalDate todayInUserZone = LocalDate.now(ZoneId.of(zoneIdStr));
        Optional<DailyQuizRecord> optionalDailyQuizRecord = dailyQuizRecordRepository.findByMemberIdAndSolvedDate(member.getId(), todayInUserZone);

        if (optionalDailyQuizRecord.isEmpty()) {
            return true;
        }

        DailyQuizRecord dailyQuizRecord = optionalDailyQuizRecord.get();
        return !dailyQuizRecord.getIsDailyQuizComplete();
    }
}

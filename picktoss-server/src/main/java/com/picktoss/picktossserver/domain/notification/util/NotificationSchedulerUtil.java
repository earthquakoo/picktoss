package com.picktoss.picktossserver.domain.notification.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.domain.admin.util.AdminNotificationUtil;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.notification.entity.Notification;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.domain.quiz.util.QuizUtil;
import com.picktoss.picktossserver.global.enums.notification.NotificationStatus;
import com.picktoss.picktossserver.global.enums.notification.NotificationTarget;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSchedulerUtil {

    private final RedisUtil redisUtil;
    private final TaskScheduler taskScheduler;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final AdminNotificationUtil adminNotificationUtil;
    private final QuizSetRepository quizSetRepository;
    private final QuizUtil quizUtil;
    private final CollectionRepository collectionRepository;

    private Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void initNotificationSchedule() {
        List<Notification> notifications = notificationRepository.findAllByNotificationStatusAndIsActiveTrue(NotificationStatus.PENDING);

        for (Notification notification : notifications) {
            if (!notification.getNotificationTime().isBefore(LocalDateTime.now())) {
                scheduleTask(notification, notification.getNotificationTime());
            }
        }
    }

    public void scheduleTask(Notification notification, LocalDateTime notificationTime) {
        ScheduledFuture<?> schedule = taskScheduler.schedule(() -> handleNotification(notification), adminNotificationUtil.toInstant(notificationTime));
        scheduledTasks.put(notification.getId(), schedule);
        System.out.println("scheduledTasks = " + scheduledTasks);
    }

    public void cancelScheduleTask(Long notificationId) {
        ScheduledFuture<?> schedule = scheduledTasks.get(notificationId);
        scheduledTasks.remove(notificationId);
        schedule.cancel(true);
        System.out.println("scheduledTasks = " + scheduledTasks);
    }

    private void handleNotification(Notification notification) {

        createNotificationForRedis(notification.getNotificationKey(), notification.getTitle(), notification.getContent(), notification.getNotificationTime(), notification.getNotificationType());
        // 알림 발송
        sendNotification(notification).run();

        List<DayOfWeek> repeatDays = adminNotificationUtil.stringsToDayOfWeeks(notification.getRepeatDays());
        if (repeatDays != null && !repeatDays.isEmpty()) {
            // 다음 알림 예약
            DayOfWeek nextDay = adminNotificationUtil.findNextDay(repeatDays, notification.getNotificationTime().getDayOfWeek());
            LocalDateTime nextNotificationTime = adminNotificationUtil.calculateNextNotificationTime(notification.getNotificationTime(), nextDay);
            System.out.println("nextNotificationTime = " + nextNotificationTime);
            updateNotificationTimeBySendPushNotification(notification.getId(), nextNotificationTime);
            updateNotificationStatusPendingBySendPushNotification(notification.getId());
            updateNotificationKeyBySendPushNotification(notification.getId());
            scheduleTask(notification, nextNotificationTime);
        } else {
            updateNotificationIsActiveBySendPushNotification(notification.getId());
        }
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

    @Transactional
    private void updateNotificationTimeBySendPushNotification(Long notificationId, LocalDateTime sendTime) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        notification.updateNotificationSendTime(sendTime);
        notificationRepository.save(notification);
    }

    @Transactional
    private void updateNotificationIsActiveBySendPushNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorInfo.NOTIFICATION_NOT_FOUND));

        notification.updateNotificationIsActiveFalse();
        notificationRepository.save(notification);
    }

    private Runnable sendNotification(Notification notification) {
        return () -> {
            List<Member> members = memberRepository.findAllByIsQuizNotificationEnabledTrue();
            for (Member member : members) {
                if (!filterNotificationTarget(notification.getNotificationType(), notification.getNotificationTarget(), member)) continue;

                addNotificationReceivedMemberData(member.getId(), notification.getNotificationKey());

                Optional<String> optionalToken = redisUtil.getData(RedisConstant.REDIS_FCM_PREFIX, member.getId().toString(), String.class);
                if (optionalToken.isEmpty()) {
                    continue;
                }
                String fcmToken = optionalToken.get();

                Message message = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(
                                com.google.firebase.messaging.Notification.builder()
                                        .setTitle(notification.getTitle())
                                        .setBody(notification.getContent())
                                        .build()
                        )
                        .setAndroidConfig(AndroidConfig.builder()
                                .setNotification(
                                        AndroidNotification.builder()
                                                .setTitle(notification.getTitle())
                                                .setBody(notification.getContent())
                                                .setClickAction("push_click")
                                                .build())
                                .build()
                        )
                        .setApnsConfig(ApnsConfig.builder()
                                .setAps(Aps.builder()
                                        .setAlert(ApsAlert.builder()
                                                .setTitle(notification.getTitle())
                                                .setBody(notification.getContent())
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
            updateNotificationStatusCompleteBySendPushNotification(notification.getId());
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

    private boolean filterNotificationTarget(NotificationType notificationType, NotificationTarget notificationTarget, Member member) {
        if (notificationTarget == NotificationTarget.ALL) {
            return true;
        }

        if (notificationType == NotificationType.TODAY_QUIZ) {
            return notificationTargetByTodayQuiz(notificationTarget, member);
        } else if (notificationType == NotificationType.COLLECTION) {
            if (notificationTarget == NotificationTarget.COLLECTION_NOT_GENERATE) {
                return checkNotificationTargetByCollectionNotGenerate(member);
            }
            return checkNotificationTargetByInterestCollection(notificationTarget, member);
        }

        return true;
    }

    private boolean notificationTargetByTodayQuiz(NotificationTarget notificationTarget, Member member) {
        List<QuizSet> quizSets = quizSetRepository.findAllByMemberIdAndSolvedTrueAndTodayQuizSetOrderByCreatedAtDesc(member.getId());

        if (notificationTarget == NotificationTarget.QUIZ_INCOMPLETE_STATUS) {
            return quizUtil.checkTodayQuizSetSolvedStatus(quizSets);
        }

        return quizUtil.checkConsecutiveUnsolvedQuizSetsOverFourDays(quizSets);
    }

    private boolean checkNotificationTargetByCollectionNotGenerate(Member member) {
        List<Collection> collections = collectionRepository.findAllByMemberId(member.getId());
        if (collections.isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean checkNotificationTargetByInterestCollection(NotificationTarget notificationTarget, Member member) {
        List<String> collectionFields = member.getInterestCollectionCategories();
        for (String collectionFieldString : collectionFields) {
            String notificationTargetString = notificationTarget.toString();

            if (notificationTargetString.equals(collectionFieldString)) {
                return true;
            }
        }
        return false;
    }
}

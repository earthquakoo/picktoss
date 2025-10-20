package com.picktoss.picktossserver.domain.notification.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import com.picktoss.picktossserver.core.messagesource.MessageService;
import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSendUtil {

    private final RedisUtil redisUtil;
    private final MessageService messageService;

    public void sendNotificationByStarReward(Long memberId) {
        String title = messageService.getMessage("notification.star_reward.title");
        String content = messageService.getMessage("notification.star_reward.content");
//        String title = "🌟 친구 초대 보상 도착";
//        String content = "받은 별을 확인해 보세요!";

        Optional<String> optionalToken = redisUtil.getData(RedisConstant.REDIS_FCM_PREFIX, memberId.toString(), String.class);
        if (optionalToken.isEmpty()) {
            return ;
        }
        String fcmToken = optionalToken.get();

        Message message = Message.builder()
                .setToken(fcmToken)
                .putData("title", title)
                .putData("content", content)
                .setNotification(
                        com.google.firebase.messaging.Notification.builder()
                                .setTitle(title)
                                .setBody(content)
                                .build()
                )
                .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(
                                AndroidNotification.builder()
                                        .setTitle(title)
                                        .setBody(content)
                                        .setClickAction("push_click")
                                        .build())
                        .build()
                )
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setAlert(ApsAlert.builder()
                                        .setTitle(title)
                                        .setBody(content)
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

    private void createNotificationForRedis(String notificationKey, String title, String content, LocalDateTime notificationSendTime, NotificationType notificationType) {
        LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

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
                "createdAt", LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        );
        redisUtil.setData(RedisConstant.REDIS_NOTIFICATION_RECEIVED_MEMBER_PREFIX, memberIdKey, notificationReceivedMemberData);
    }
}

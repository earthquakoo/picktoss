package com.picktoss.picktossserver.domain.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.domain.notification.dto.GetNotificationsResponse;
import com.picktoss.picktossserver.global.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSearchService {

    private final RedisUtil redisUtil;

    public List<GetNotificationsResponse.GetNotificationsDto> findNotifications(Long memberId) {
        String memberIdKey = memberId.toString();

        List<GetNotificationsResponse.GetNotificationsDto> notificationsDtos = new ArrayList<>();

        Optional<Map> existingReceivedMemberData = redisUtil.getData(RedisConstant.REDIS_NOTIFICATION_RECEIVED_MEMBER_PREFIX, memberIdKey, Map.class);
        if (existingReceivedMemberData.isEmpty()) {
            createNotificationReceivedMemberData(memberIdKey);
            return notificationsDtos;
        }

        Map notificationReceivedMemberData = existingReceivedMemberData.get();
        Object notificationKeysObject = notificationReceivedMemberData.get("notificationKeys");
        List<String> notificationKeys = new ObjectMapper().convertValue(notificationKeysObject, new TypeReference<List<String>>() {});
        for (String notificationKey : notificationKeys) {
            Optional<Map> optionalNotificationData = redisUtil.getData(RedisConstant.REDIS_NOTIFICATION_PREFIX, notificationKey, Map.class);
            if (optionalNotificationData.isEmpty()) continue;

            Map notificationData = optionalNotificationData.get();
            LocalDateTime notificationSendTime = LocalDateTime.parse(notificationData.get("notificationSendTime").toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            NotificationType notificationType = NotificationType.valueOf(notificationData.get("notificationType").toString());

            GetNotificationsResponse.GetNotificationsDto notificationsDto = GetNotificationsResponse.GetNotificationsDto.builder()
                    .title(notificationData.get("title").toString())
                    .content(notificationData.get("content").toString())
                    .notificationKey(notificationKey)
                    .notificationType(notificationType)
                    .receivedTime(notificationSendTime)
                    .build();

            notificationsDtos.add(notificationsDto);
        }
        return notificationsDtos;
    }

    public void checkNotification(Long memberId, String notificationKey) {
        String memberIdKey = memberId.toString();

        Optional<Map> existingReceivedMemberData = redisUtil.getData(RedisConstant.REDIS_NOTIFICATION_RECEIVED_MEMBER_PREFIX, memberIdKey, Map.class);
        if (existingReceivedMemberData.isEmpty()) {
            return ;
        }

        if (existingReceivedMemberData.isPresent()) {
            Map notificationReceivedMemberData = existingReceivedMemberData.get();
            Object notificationKeysObject = notificationReceivedMemberData.get("notificationKeys");

            List<String> notificationKeys = new ObjectMapper().convertValue(notificationKeysObject, new TypeReference<List<String>>() {});
            notificationKeys.remove(notificationKey);

            notificationReceivedMemberData.put("notificationKeys", notificationKeys);
            redisUtil.setData(RedisConstant.REDIS_NOTIFICATION_RECEIVED_MEMBER_PREFIX, memberIdKey, notificationReceivedMemberData);
        }
    }

    private void createNotificationReceivedMemberData(String memberIdKey) {
        List<String> notificationKeys = new ArrayList<>();
        Map<String, Object> notificationReceivedMemberData = Map.of(
                "notificationKeys", notificationKeys,
                "createdAt", LocalDateTime.now()
        );

        redisUtil.setData(RedisConstant.REDIS_NOTIFICATION_RECEIVED_MEMBER_PREFIX, memberIdKey, notificationReceivedMemberData);
    }
}

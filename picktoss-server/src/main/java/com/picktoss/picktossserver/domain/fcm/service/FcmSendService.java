package com.picktoss.picktossserver.domain.fcm.service;

import com.google.firebase.messaging.*;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmSendService {

    private final RedisUtil redisUtil;

    public void sendByToken(String title, String body, String content, Long memberId) {
        Optional<String> optionalToken = redisUtil.getData(RedisConstant.REDIS_FCM_PREFIX, memberId.toString(), String.class);
        if (optionalToken.isEmpty()) {
            throw new CustomException(ErrorInfo.FCM_TOKEN_NOT_FOUND);
        }
        String fcmToken = optionalToken.get();

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                        Notification.builder()
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
}

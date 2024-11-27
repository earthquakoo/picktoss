package com.picktoss.picktossserver.domain.fcm.service;

import com.google.firebase.messaging.*;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final RedisUtil redisUtil;
    private final FirebaseMessaging firebaseMessaging;

    public void saveFcmToken(Long memberId, String fcmToken) {
        redisUtil.setData(memberId.toString(), fcmToken);
    }

    public void sendByToken(String fcmToken, String title, String body, Long memberId){
//        Optional<String> optionalToken = redisUtil.getData(memberId.toString(), String.class);
//        if (optionalToken.isEmpty()) {
//            throw new CustomException(ErrorInfo.FCM_TOKEN_NOT_FOUND);
//        }
//        String fcmToken = optionalToken.get();

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .setAndroidConfig(
                        AndroidConfig.builder()
                                .setNotification(
                                        AndroidNotification.builder()
                                                .setTitle(title)
                                                .setBody(body)
                                                .setClickAction("push_click")
                                                .build()
                                )
                                .build()
                )
                .setApnsConfig(
                        ApnsConfig.builder()
                                .setAps(Aps.builder()
                                        .setCategory("push_click")
                                        .build())
                                .build()
                )
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCMsend-"+response);
        } catch (FirebaseMessagingException e) {
            log.info("FCMexcept-"+ e.getMessage());
        }
    }

    public void pushNotification(Long memberId, String fcmToken, String content) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setWebpushConfig(WebpushConfig.builder()
                            .putHeader("ttl", "300")
                            .setNotification(new WebpushNotification("Gappa", content))
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCMsend-"+response);
        } catch (Exception e) {
            log.info("FCMexcept-"+ e.getMessage());
        }
    }

//    public void pushNotification(Long memberId, String content) {
//        Map<String, Object> resultMap = new HashMap<>();
//        try {
//            Optional<String> optionalToken = redisUtil.getData(memberId.toString(), String.class);
//            if (optionalToken.isEmpty()) {
//                resultMap.put("message", "유저의 FireBase 토큰이 없습니다.");
//            }
//            else {
//                String fcmToken = optionalToken.get();
//                Message message = Message.builder()
//                        .setToken(fcmToken)
//                        .setWebpushConfig(WebpushConfig.builder()
//                                .putHeader("ttl", "300")
//                                .setNotification(new WebpushNotification("Gappa", content))
//                                .build())
//                        .build();
//                String response = FirebaseMessaging.getInstance().sendAsync(message).get();
//                log.info("FCMsend-"+response);
//            }
//        } catch (Exception e) {
//            log.info("FCMexcept-"+ e.getMessage());
//        }
//    }
}

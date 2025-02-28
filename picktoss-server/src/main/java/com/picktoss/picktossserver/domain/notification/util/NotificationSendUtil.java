package com.picktoss.picktossserver.domain.notification.util;

import com.google.firebase.messaging.*;
import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.notification.repository.NotificationRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.domain.quiz.util.QuizUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSendUtil {

    private final RedisUtil redisUtil;
    private final TaskScheduler taskScheduler;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final NotificationUtil notificationUtil;
    private final QuizSetRepository quizSetRepository;
    private final QuizUtil quizUtil;
    private final CollectionRepository collectionRepository;

    public void sendNotificationByStarReward(Long memberId) {
        String title = "친구 초대 보상입니다.";
        String content = "별 50개가 지급되었습니다!";
        Optional<String> optionalToken = redisUtil.getData(RedisConstant.REDIS_FCM_PREFIX, memberId.toString(), String.class);
        if (optionalToken.isEmpty()) {
            return ;
        }
        String fcmToken = optionalToken.get();

        Message message = Message.builder()
                .setToken(fcmToken)
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
}

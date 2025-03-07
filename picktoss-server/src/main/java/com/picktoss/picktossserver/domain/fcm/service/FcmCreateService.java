package com.picktoss.picktossserver.domain.fcm.service;

import com.picktoss.picktossserver.core.redis.RedisConstant;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmCreateService {

    private final RedisUtil redisUtil;

    public void saveFcmToken(Long memberId, String fcmToken) {
        System.out.println("fcmToken = " + fcmToken);
        redisUtil.setData(RedisConstant.REDIS_FCM_PREFIX, memberId.toString(), fcmToken);
    }
}

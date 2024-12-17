package com.picktoss.picktossserver.core.redis;

import com.picktoss.picktossserver.core.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisHandler {

    private final RedisConfig redisConfig;

    public ListOperations<String, Object> getListOperations() {
        return redisConfig.redisTemplate().opsForList();
    }

    public ValueOperations<String, Object> getValueOperations() {
        return redisConfig.redisTemplate().opsForValue();
    }

    public HashOperations<String, Object, Object> getHashOptions() {
        return redisConfig.redisTemplate().opsForHash();
    }
}

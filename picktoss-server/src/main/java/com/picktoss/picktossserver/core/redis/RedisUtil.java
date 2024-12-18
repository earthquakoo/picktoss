package com.picktoss.picktossserver.core.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class RedisUtil implements RedisManager {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public <T> Optional<T> getData(final String prefix, final String key, final Class<T> classType) {
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String prefixedKey = prefix + ":" + key;
        final String value = valueOperations.get(prefixedKey);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(objectMapper.readValue(value, classType));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void setData(final String prefix, final String key, T value) {
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String prefixedKey = prefix + ":" + key;
        try {
            valueOperations.set(prefixedKey, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void setData(final String prefix, final String key, T value, long durationMillis) {
        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String prefixedKey = prefix + ":" + key;
        final Duration expireDuration = Duration.ofMillis(durationMillis);
        try {
            valueOperations.set(prefixedKey, objectMapper.writeValueAsString(value), expireDuration);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void deleteData(final String prefix, final String key) {
        String prefixedKey = prefix + ":" + key;
        redisTemplate.delete(prefixedKey);
    }

    public static long toTomorrow() {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime tomorrow = now.plusDays(1);
        final long secondsUntilTomorrow = tomorrow.toEpochSecond(UTC) - now.toEpochSecond(UTC);
        return secondsUntilTomorrow * 1000;
    }

    public void flushAll() {
        requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands();
    }
}

package com.picktoss.picktossserver.core.redis;

import java.util.Optional;

public interface RedisManager {
    <T> Optional<T> getData(final String prefix, final String key, final Class<T> classType);

    <T> void setData(final String prefix, final String key, T value);

    <T> void setData(final String prefix, final String key, T value, final long durationMillis);

    void deleteData(final String prefix, final String key);
}

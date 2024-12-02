package com.picktoss.picktossserver.core.redis;

public final class RedisConstant {
    public static final String REDIS_PAYMENT_PREFIX = "payment";
    public static final String REDIS_INVITE_PREFIX = "invite";
    public static final String REDIS_FCM_PREFIX = "fcm";

    public static final long REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS = 259200000; //3일
}

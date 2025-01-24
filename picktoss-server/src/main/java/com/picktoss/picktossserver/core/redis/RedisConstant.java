package com.picktoss.picktossserver.core.redis;

public final class RedisConstant {
    public static final String REDIS_PAYMENT_PREFIX = "payment";
    public static final String REDIS_INVITE_CODE_PREFIX = "inviteCode";
    public static final String REDIS_INVITE_MEMBER_PREFIX = "inviteMember";
    public static final String REDIS_FCM_PREFIX = "fcm";
    public static final String REDIS_NOTIFICATION_RECEIVED_MEMBER_PREFIX = "notificationReceivedMember";
    public static final String REDIS_NOTIFICATION_PREFIX = "notification";

    public static final long REDIS_INVITE_LINK_EXPIRATION_DURATION_MILLIS = 259200000; //3일
    public static final long REDIS_NOTIFICATION_EXPIRATION_DURATION_MILLIS = 1209600033;
}

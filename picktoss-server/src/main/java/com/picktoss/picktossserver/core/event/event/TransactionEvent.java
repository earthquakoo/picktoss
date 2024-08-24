package com.picktoss.picktossserver.core.event.event;

import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TransactionEvent {
    private final Long memberId;
    private final String s3Key;
    private final Long documentId;
    private final SubscriptionPlanType subscriptionPlanType;
}
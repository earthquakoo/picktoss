package com.picktoss.picktossserver.domain.subscription.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.enums.subscription.SubscriptionPlanType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "subscription")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Subscription {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan_type", nullable = false)
    private SubscriptionPlanType subscriptionPlanType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static Subscription createSubscription(LocalDateTime expireAt, Member member) {
        return Subscription.builder()
                .subscriptionPlanType(SubscriptionPlanType.FREE)
                .createdAt(LocalDateTime.now())
                .expireAt(expireAt)
                .member(member)
                .build();
    }

    public void processSubscriptionPlanTypeDowngradeOnExpiration() {
        this.subscriptionPlanType = SubscriptionPlanType.FREE;
        this.expireAt = null;
    }

    public void processSubscriptionPlanTypeUpgradeOnPayment(LocalDateTime expireAt) {
        this.subscriptionPlanType = SubscriptionPlanType.PRO;
        this.expireAt = expireAt;
    }
}
package com.picktoss.picktossserver.domain.subscription.entity;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.FREE_PLAN_AI_PICK_LIMIT_EXCEED_ERROR;

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

    @Column(name = "available_ai_pick_count", nullable = false)
    private int availableAiPickCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private SubscriptionPlanType subscriptionPlanType;

    @CreatedDate
    @Column(name = "purchased_date", nullable = false)
    private LocalDateTime purchasedDate;

    @Column(name = "expire_date", nullable = false)
    private LocalDateTime expireDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void minusAvailableAiPickCount() {
        this.availableAiPickCount -= 1;
    }
}

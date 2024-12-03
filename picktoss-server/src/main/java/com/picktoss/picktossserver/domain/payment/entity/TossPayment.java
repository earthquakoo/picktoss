package com.picktoss.picktossserver.domain.payment.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.enums.payment.PaymentMethod;
import com.picktoss.picktossserver.global.enums.payment.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "toss_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TossPayment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "payment_key", nullable = false)
    private String paymentKey;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "request_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approve_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static TossPayment createTossPayment(
            String paymentKey, String orderId, PaymentMethod paymentMethod, PaymentStatus paymentStatus, Integer amount, LocalDateTime requestedAt, LocalDateTime approvedAt, Member member) {
        return TossPayment.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .amount(amount)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .member(member)
                .build();
    }

}

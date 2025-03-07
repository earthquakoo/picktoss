package com.picktoss.picktossserver.domain.payment.entity;

import com.picktoss.picktossserver.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "imp_uid", nullable = false)
    private String impUid; //포트원 결제 고유 번호

    @Column(name = "merchant_uid", nullable = false)
    private String merchantUid; //주문 번호

//    @Column(name = "pay_method", nullable = false)
//    private String payMethod;
//
//    @Column(name = "paid_at", nullable = false)
//    private LocalDateTime paidAt;
//
//    @Column(name = "next_billing_date")
//    private LocalDateTime nextBillingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Constructor methods
    public static Payment createPayment(String impUid, Integer amount, Member member) {
        return Payment.builder()
                .amount(amount)
                .impUid(impUid)
                .member(member)
                .build();
    }
}

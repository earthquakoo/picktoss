package com.picktoss.picktossserver.domain.member.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.member.WithdrawalReasonContent;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "withdrawal_reason")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WithdrawalReason extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private WithdrawalReasonContent reason;

    private String detail;

    public static WithdrawalReason createWithdrawalReason(WithdrawalReasonContent reason, String detail) {
        return WithdrawalReason.builder()
                .reason(reason)
                .detail(detail)
                .build();
    }
}

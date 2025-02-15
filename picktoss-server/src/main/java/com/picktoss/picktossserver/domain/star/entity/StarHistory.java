package com.picktoss.picktossserver.domain.star.entity;

import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.star.Source;
import com.picktoss.picktossserver.global.enums.star.TransactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "star_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StarHistory extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    //포인트 사용 설명
    @Column(name = "description")
    private String description;

    //포인트 증감량
    @Column(name = "change_amount")
    private Integer changeAmount;

    //트랜잭션 이후 포인트 잔액
    @Column(name = "balance_after")
    private Integer balanceAfter;

    //포인트 변경 사유
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    //포인트 적립/사용 출처
    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private Source source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "star_id", nullable = false)
    private Star star;


    public static StarHistory createStarHistory(
            String description, Integer changeAmount, Integer balanceAfter, TransactionType transactionType, Source source, Star star
    ) {
        return StarHistory.builder()
                .description(description)
                .changeAmount(changeAmount)
                .balanceAfter(balanceAfter)
                .transactionType(transactionType)
                .source(source)
                .star(star)
                .build();
    }
}

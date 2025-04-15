package com.picktoss.picktossserver.domain.star.entity;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.star.constant.StarConstant;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
import com.picktoss.picktossserver.global.enums.star.Source;
import com.picktoss.picktossserver.global.enums.star.TransactionType;
import com.picktoss.picktossserver.global.enums.subscription.SubscriptionPlanType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "star")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Star extends AuditBase {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "star", nullable = false)
    private Integer star;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "is_unlimited", nullable = false)
    private Boolean isUnlimited;

    @OneToMany(mappedBy = "star", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StarHistory> starHistories = new ArrayList<>();

    public static Star createStar(Integer star, Member member) {
        return Star.builder()
                .star(star)
                .member(member)
                .isUnlimited(false)
                .build();
    }

    public StarHistory withdrawalStarByCreateDocument(Star star, Integer starCount, SubscriptionPlanType subscriptionPlanType) {
        if (star.getIsUnlimited() && subscriptionPlanType == SubscriptionPlanType.PRO) {
            starCount = 0;
        }

        if (star.getStar() < starCount) {
            throw new CustomException(ErrorInfo.STAR_SHORTAGE_IN_POSSESSION);
        }

        Integer curStarCount = star.getStar();
        Integer changeStarCount = curStarCount - starCount;

        StarHistory starHistory = StarHistory.createStarHistory("문서 생성으로 인한 지출", starCount, changeStarCount, TransactionType.WITHDRAWAL, Source.SERVICE, star);

        this.star -= starCount;
        return starHistory;
    }

    public StarHistory depositStarByQuizSetSolvedReward(Star star, int reward) {
        Integer curStarCount = star.getStar();
        Integer changeStarCount = curStarCount + reward;

        StarHistory starHistory = StarHistory.createStarHistory("퀴즈셋 풀이 보상", reward, changeStarCount, TransactionType.DEPOSIT, Source.REWARD, star);

        this.star += reward;
        return starHistory;
    }

    public StarHistory depositStarBySolvedDailyQuizReward(Star star, int reward) {
        Integer curStarCount = star.getStar();
        Integer changeStarCount = curStarCount + reward;

        StarHistory starHistory = StarHistory.createStarHistory("데일리 퀴즈 보상", reward, changeStarCount, TransactionType.DEPOSIT, Source.REWARD, star);

        this.star += reward;
        return starHistory;
    }

    public StarHistory depositStarByInviteFriendReward(Star star) {
        StarHistory lastStarHistory = star.getStarHistories().getLast();
        Integer changeAmount = StarConstant.INVITE_FRIEND_REWARD;
        Integer balanceAfter = lastStarHistory.getBalanceAfter() + changeAmount;

        StarHistory starHistory = StarHistory.createStarHistory("친구 초대 보상", changeAmount, balanceAfter, TransactionType.DEPOSIT, Source.REWARD, star);

        this.star += StarConstant.INVITE_FRIEND_REWARD;
        return starHistory;
    }

    public void updateIsUnlimitedStarsForSubscriptionProType() {
        this.isUnlimited = true;
    }
}

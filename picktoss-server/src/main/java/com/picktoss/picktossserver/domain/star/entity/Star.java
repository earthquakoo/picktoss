package com.picktoss.picktossserver.domain.star.entity;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.global.baseentity.AuditBase;
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

    @OneToMany(mappedBy = "star", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StarHistory> starHistories = new ArrayList<>();

    public static Star createStar(Integer star, Member member) {
        return Star.builder()
                .star(star)
                .member(member)
                .build();
    }

    public void withdrawalStarByCreateDocument(Integer star) {
        if (this.star < star) {
            throw new CustomException(ErrorInfo.STAR_SHORTAGE_IN_POSSESSION);
        }
        this.star -= star;
    }

    public void depositStarByTodayQuizSolvedReward(Integer star) {
        this.star += star;
    }

    public void depositStarByInviteFriendReward(Integer star) {
        this.star += star;
    }

    public void depositStarByInvalidQuizReward(Integer star) {
        this.star += star;
    }
}

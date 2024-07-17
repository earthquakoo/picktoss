package com.picktoss.picktossserver.domain.member.controller.response;

import com.picktoss.picktossserver.global.enums.MemberRole;
import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetMemberInfoResponse {

    private Long id;
    private String name;
    private String email;
    private MemberRole role;
    private int point;
    private int continuousQuizDatesCount;
    private int maxContinuousQuizDatesCount;
    private GetMemberInfoSubscriptionDto subscription;
    private GetMemberInfoDocumentDto documentUsage;
    private boolean isQuizNotificationEnabled;

    @Getter
    @Builder
    public static class GetMemberInfoSubscriptionDto {
        private SubscriptionPlanType plan;
        private LocalDateTime purchasedDate;
        private LocalDateTime expireDate;
    }

    @Getter
    @Builder
    public static class GetMemberInfoDocumentDto {
        // 보유한 문서 개수
        private int possessDocumentCount;
        // 구독 기간 동안 사용가능한 AI PICK 횟수
        private int availableAiPickCount;
        // Free 플랜 최대 문서 보유 개수 20
        private int freePlanMaxPossessDocumentCount;
        // Free 플랜 매달 사용가능한 AI PICK 횟수
        private int freePlanMonthlyAvailableAiPickCount;
        // Pro 플랜 매달 사용가능한 AI PICK 횟수
        private int proPlanMonthlyAvailableAiPickCount;
    }
}

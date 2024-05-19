package com.picktoss.picktossserver.domain.member.controller.response;

import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetMemberInfoResponse {

    private String name;
    private String email;
    private int point;
    private int continuousQuizDatesCount;
    private GetMemberInfoSubscriptionDto subscription;
    private GetMemberInfoDocumentDto documentUsage;

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
        // 구독 기간 동안 업로드할 수 있는 문서 개수
        private int possibleUploadedDocumentCount;
        // Free 플랜 최대 문서 보유 개수 30
        private int freePlanMaxPossessDocumentCount;
        // Free 플랜 매달 업로드 가능한 문서 개수
        private int freePlanMonthlyDocumentCount;
        // Pro 플랜 매달 업로드 가능한 문서 개수
        private int proPlanMonthlyDocumentCount;
    }

}

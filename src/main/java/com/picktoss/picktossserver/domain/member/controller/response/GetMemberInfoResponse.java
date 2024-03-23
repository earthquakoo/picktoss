package com.picktoss.picktossserver.domain.member.controller.response;

import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetMemberInfoResponse {

    private String email;
    private QuizDto quiz;
    private SubscriptionDto subscription;
    private DocumentDto documentUsage;

    @Getter
    @Builder
    public static class QuizDto {
        private int freePlanQuizQuestionNum;
        private int proPlanQuizQuestionNum;
    }

    @Getter
    @Builder
    public static class SubscriptionDto {
        private SubscriptionPlanType planType;
        private LocalDateTime purchasedDate;
        private LocalDateTime expireDate;
    }

    @Getter
    @Builder
    public static class DocumentDto {
        //현재 업로드된 문서 개수
        private int currentPossessDocumentNum;
        //현재 구독 기간 동안 업로드한 문서 개수
        private int currentSubscriptionCycleUploadedDocumentNum;
        //Free 플랜 최대 문서 보유 개수
        private int freePlanMaxPossessDocumentNum;
        //Free 플랜 구독 기간 최대 업로드 문서 개수
        private int freePlanSubscriptionMaxUploadDocumentNum;
        //Pro 플랜 최대 문서 보유 개수
        private int proPlanMaxPossessDocumentNum;
        //Pro 플랜 구독 기간 최대 업로드 문서 개수
        private int proPlanSubscriptionMaxUploadDocumentNum;
    }

}

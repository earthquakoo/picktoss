package com.picktoss.picktossserver.domain.document.constant;

import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;

public final class DocumentConstant {

    public static final int DOCUMENT_MAX_LEN = 15000;
    public static final int DOCUMENT_MIN_LEN = 500;

    // 보유할 수 있는 문서 개수
    public static final int FREE_PLAN_MAX_POSSESS_DOCUMENT_COUNT = 30;

    // 기본으로 업로드할 수 있는 문서 개수
    public static final int FREE_PLAN_DEFAULT_DOCUMENT_COUNT = 20;

    // 매 달 생성할 수 있는 문서 개수
    public static final int FREE_PLAN_MONTHLY_DOCUMENT_COUNT = 5;
    public static final int PRO_PLAN_MONTHLY_DOCUMENT_COUNT = 20;

    //현재 구독 사이클에 업로드할 수 있는 문서 최대 개수
    public static final int FREE_PLAN_MONTHLY_MAX_DOCUMENT_NUM = 15;
    public static final int PRO_PLAN_MONTHLY_MAX_DOCUMENT_NUM = 40;

    //매 시점에 업로드될 수 있는 문서 최대 개수
    public static final int FREE_PLAN_CURRENT_MAX_DOCUMENT_NUM = 10;
    public static final int PRO_PLAN_CURRENT_MAX_DOCUMENT_NUM = 40;

    public int findCurrentSubscriptionMaxDocumentNumBySubscriptionPlan(SubscriptionPlanType planType) {
        if (planType == SubscriptionPlanType.FREE) {
            return FREE_PLAN_MONTHLY_MAX_DOCUMENT_NUM;
        } else if (planType == SubscriptionPlanType.PRO) {
            return PRO_PLAN_MONTHLY_MAX_DOCUMENT_NUM;
        } else {
            throw new IllegalArgumentException("Invalid subscription plan type");
        }
    }

    public int findAnytimeMaxDocumentNumBySubscriptionPlan(SubscriptionPlanType planType) {
        if (planType == SubscriptionPlanType.FREE) {
            return FREE_PLAN_CURRENT_MAX_DOCUMENT_NUM;
        } else if (planType == SubscriptionPlanType.PRO) {
            return PRO_PLAN_CURRENT_MAX_DOCUMENT_NUM;
        } else {
            throw new IllegalArgumentException("Invalid subscription plan type");
        }
    }
}

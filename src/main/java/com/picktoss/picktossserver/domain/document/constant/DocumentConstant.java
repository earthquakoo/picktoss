package com.picktoss.picktossserver.domain.document.constant;

import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;

public final class DocumentConstant {


    public static final int DOCUMENT_MAX_LEN = 15000;
    public static final int DOCUMENT_MIN_LEN = 500;

    // 보유할 수 있는 문서 개수
    public static final int FREE_PLAN_MAX_POSSESS_DOCUMENT_COUNT = 20;

    // 기본으로 업로드할 수 있는 문서 개수
    public static final int FREE_PLAN_DEFAULT_DOCUMENT_COUNT = 15;

    // 매 달 생성할 수 있는 문서 개수
    public static final int FREE_PLAN_MONTHLY_DOCUMENT_COUNT = 5;
    public static final int PRO_PLAN_MONTHLY_DOCUMENT_COUNT = 20;

}

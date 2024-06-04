package com.picktoss.picktossserver.domain.document.constant;

import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;

public final class DocumentConstant {


    public static final int DOCUMENT_MAX_LEN = 15000;
    public static final int DOCUMENT_MIN_LEN = 500;

    // AI Pick을 이용할 수 있는 횟수
    public static final int AVAILABLE_AI_PICK_COUNT = 15;

    // 보유할 수 있는 문서 개수
    public static final int FREE_PLAN_MAX_POSSESS_DOCUMENT_COUNT = 20;

    // 기본으로 업로드할 수 있는 문서 개수
    public static final int FREE_PLAN_DEFAULT_DOCUMENT_COUNT = 15;

    // 매 달 초기화되는 AI Pick 이용 횟수
    public static final int FREE_PLAN_MONTHLY_AVAILABLE_AI_PICK_COUNT = 5;
    public static final int PRO_PLAN_MONTHLY_AVAILABLE_AI_PICK_COUNT = 20;
    public static final int FREE_PLAN_MONTHLY_DOCUMENT_COUNT = 5;
    public static final int PRO_PLAN_MONTHLY_DOCUMENT_COUNT = 20;

    // Default document s3Key
    public static final String DEFAULT_DOCUMENT_S3KEY = "asd";
}

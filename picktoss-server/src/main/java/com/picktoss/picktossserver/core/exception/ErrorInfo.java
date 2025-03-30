package com.picktoss.picktossserver.core.exception;

import lombok.Getter;

@Getter
public enum ErrorInfo {
    /**
     * Authentication
     **/
    MEMBER_NOT_FOUND(400, "MEMBER_NOT_FOUND", "해당 id의 사용자를 찾을 수 없습니다."),
    EMAIL_VERIFICATION_NOT_FOUND(400, "EMAIL_VERIFICATION_NOT_FOUND", "해당 이메일에 대한 이메일 인증을 찾을 수 없습니다."),
    EMAIL_ALREADY_VERIFIED(400, "EMAIL_ALREADY_VERIFIED", "이메일이 이미 인증되었습니다."),
    INVALID_VERIFICATION_CODE(400, "INVALID_VERIFICATION_CODE", "잘못된 인증 코드입니다."),
    VERIFICATION_CODE_EXPIRED(400, "VERIFICATION_CODE_EXPIRED", "이메일 인증 코드가 만료되었습니다."),
    INVALID_SOCIAL_PLATFORM(400, "INVALID_SOCIAL_PLATFORM", "잘못된 소셜 플랫폼입니다."),
    INVITE_LINK_EXPIRED_OR_NOT_FOUND(400, "INVITE_LINK_EXPIRED_OR_NOT_FOUND", "초대 링크가 만료되었거나 잘못된 초대 링크입니다."),
    FCM_TOKEN_NOT_FOUND(400, "FCM_TOKEN_NOT_FOUND", "FCM 토큰을 찾을 수 없습니다."),
    INVALID_PASSWORD(400, "INVALID_PASSWORD", "비밀번호가 틀렸습니다."),
    INVITED_MEMBER_NOT_FOUND(400, "INVITED_MEMBER_NOT_FOUND", "초대한 사용자를 찾을 수 없습니다."),
    ALREADY_USED_INVITED_CODE(400, "ALREADY_USED_INVITE_CODE", "이미 사용된 초대 코드입니다."),


    /**
     * AWS Exception
     */
    AMAZON_SERVICE_EXCEPTION(400, "AMAZON_SERVICE_EXCEPTION", "AWS 서비스 문제가 발생했습니다."),
    FILE_UPLOAD_ERROR(400, "FILE_UPLOAD_ERROR", "파일을 업로드하는데 문제가 발생했습니다."),

    /**
     * Global Exceptions
     **/
    UNAUTHORIZED_OPERATION_EXCEPTION(403, "UNAUTHORIZED_OPERATION_EXCEPTION", "해당 작업에 대한 권한이 없습니다."),
    UNABLE_TO_CONVERT_LIST_TO_STRING(400, "UNABLE_TO_CONVERT_LIST_TO_STRING", "리스트를 문자로 변환할 수 없습니다."),
    UNABLE_TO_CONVERT_STRING_TO_LIST(400, "UNABLE_TO_CONVERT_STRING_TO_LIST", "문자를 리스트로 변환할 수 없습니다."),

    /**
     * Directory
     **/
    DIRECTORY_NOT_FOUND(400, "DIRECTORY_NOT_FOUND", "해당 id의 디렉토리를 찾을 수 없습니다."),

    /**
     * Document
     **/
    DOCUMENT_NOT_FOUND(400, "DOCUMENT_NOT_FOUND", "해당 id의 문서를 찾을 수 없습니다."),
    DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR(400, "DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR", "등록할 수 있는 문서의 최대 개수를 초과했습니다."),

    /**
     * PublicQuizSet
     */
    PUBLIC_QUIZ_SET_NOT_FOUND(400, "PUBLIC_QUIZ_SET_NOT_FOUND", "해당 id의 공개된 퀴즈를 찾을 수 없습니다."),
    PUBLIC_QUIZ_SET_BOOKMARK_NOT_FOUND(400, "PUBLIC_QUIZ_SET_BOOKMARK_NOT_FOUND", "해당 id의 공개된 퀴즈 북마크를 찾을 수 없습니다."),

    /**
     * Outbox
     */
    OUTBOX_NOT_FOUND(400, "OUTBOX_NOT_FOUND", "Outbox not found."),

    /**
     * JWT Exceptions
     **/
    INVALID_JWT_TOKEN(401, "INVALID_JWT_TOKEN", "JWT 토큰이 잘못되었습니다."),
    EXPIRED_JWT_TOKEN(401, "EXPIRED_JWT_TOKEN", "JWT 토큰이 만료되었습니다."),

    /**
     * Collection
     */
    COLLECTION_NOT_FOUND(400, "COLLECTION_NOT_FOUND", "해당 id의 컬렉션을 찾을 수 없습니다."),
    COLLECTION_BOOKMARK_NOT_FOUND(400, "COLLECTION_BOOKMARK_NOT_FOUND", "해당 컬렉션 북마크를 찾을 수 없습니다."),
    DUPLICATE_QUIZ_IN_COLLECTION(400, "DUPLICATE_QUIZ_IN_COLLECTION", "해당 퀴즈는 이미 컬렉션에 포함되어 있습니다."),
    OWN_COLLECTION_CANT_BOOKMARK(400, "OWN_COLLECTION_CANT_BOOKMARK", "자신의 컬렉션은 북마크할 수 없습니다."),
    COLLECTION_ALREADY_BOOKMARKED(400, "COLLECTION_ALREADY_BOOKMARK", "이미 북마크된 컬렉션입니다."),
    INTEREST_COLLECTION_FIELD_NOT_FOUND(400, "INTEREST_COLLECTION_FIELD_NOT_FOUND", "관심분야 설정이 되어있지 않습니다"),

    /**
     * Star
     */
    STAR_SHORTAGE_IN_POSSESSION(400, "STAR_SHORTAGE_IN_POSSESSION", "보유한 별이 부족합니다."),

    /**
     * Subscription
     */
    SUBSCRIPTION_NOT_FOUND(400, "SUBSCRIPTION_NOT_FOUND", "해당 사용자 id의 구독권을 찾을 수 없습니다."),

    /**
     * Notification
     */
    NOTIFICATION_NOT_FOUND(400, "NOTIFICATION_NOT_FOUND", "해당 id의 알림을 찾을 수 없습니다."),
    INVALID_NOTIFICATION_TIME(400, "INVALID_NOTIFICATION_TIME", "알림 시간을 과거로 설정할 수 없습니다"),

    /**
     * Payment
     */
    PAYMENT_AMOUNT_ERROR(400, "PAYMENT_AMOUNT_ERROR", "결제 전과 결제 후의 요청한 금액이 상이합니다."),
    PAYMENT_AMOUNT_DIFFERENT_FROM_IAMPORT_SERVER(400, "PAYMENT_AMOUNT_DIFFERENT_FROM_IAMPORT_SERVER", "결제된 금액과 아임포트 서버 내역의 금액이 다릅니다."),
    PAYMENT_AMOUNT_DIFFERENT_FROM_DB(400, "PAYMENT_AMOUNT_DIFFERENT_FROM_DB", "결제된 금액이 DB에서 설정된 금액과 다릅니다."),
    PAYMENT_NOT_COMPLETED(400, "PAYMENT_NOT_COMPLETED", "결제가 완료되지 않았습니다."),


    /**
     * Quiz
     */
    QUIZ_NOT_FOUND_ERROR(400, "QUIZ_NOT_FOUND", "해당 id의 퀴즈를 찾을 수 없습니다."),
    QUIZ_COUNT_EXCEEDED(400, "QUIZ_COUNT_EXCEEDED", "생성된 퀴즈 수보다 더 많은 퀴즈를 생성할 수 없습니다."),
    UNRESOLVED_QUIZ_SET(400, "UNRESOLVED_QUIZ_SET", "풀지 않은 퀴즈셋입니다."),
    QUIZ_SET_TYPE_ERROR(400, "QUIZ_SET_TYPE_ERROR", "잘못된 퀴즈 세트 타입입니다."),
    QUIZ_TYPE_NOT_IN_DOCUMENT(400, "QUIZ_TYPE_NOT_IN_DOCUMENT", "문서에 없는 퀴즈 유형입니다."),
    QUIZ_SET_NOT_FOUND_ERROR(400, "QUIZ_SET_NOT_FOUND", "해당 id의 퀴즈셋을 찾을 수 없습니다.");


    private final int statusCode;
    private final String errorCode;
    private final String message;

    ErrorInfo(int statusCode, String errorCode, String message) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
    }
}

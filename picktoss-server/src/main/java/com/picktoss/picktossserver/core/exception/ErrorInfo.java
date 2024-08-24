package com.picktoss.picktossserver.core.exception;

import lombok.Getter;

@Getter
public enum ErrorInfo {
    /**
     * Authentication
     **/
    MEMBER_ALREADY_EXIST(400, "MEMBER_ALREADY_EXIST", "Member with the given email already exists."),
    MEMBER_NOT_FOUND(400, "MEMBER_NOT_FOUND", "Member with the given email does not exist."),
    EMAIL_VERIFICATION_NOT_FOUND(400, "EMAIL_VERIFICATION_NOT_FOUND", "Email verification with the given email is not found."),
    EMAIL_ALREADY_VERIFIED(400, "EMAIL_ALREADY_VERIFIED", "Email address is already verified."),
    EMAIL_NOT_VERIFIED(400, "EMAIL_NOT_VERIFIED", "Email is not verified."),
    INVALID_VERIFICATION_CODE(400, "INVALID_VERIFICATION_CODE", "Invalid verification code."),
    VERIFICATION_CODE_EXPIRED(400, "VERIFICATION_CODE_EXPIRED", "Verification code is expired"),
    INVALID_PASSWORD(400, "INVALID_PASSWORD", "Password is invalid."),

    /**
     * AWS Exception
     */
    AMAZON_SERVICE_EXCEPTION(400, "AMAZON_SERVICE_EXCEPTION", "Amazon service error."),
    FILE_UPLOAD_ERROR(400, "FILE_UPLOAD_ERROR", "There was a problem uploading the file."),

    /**
     * Category
     **/
    DUPLICATE_CATEGORY(400, "DUPLICATE_CATEGORY", "Parent category cannot have multiple child categories with the same name"),
    CATEGORY_NOT_FOUND(400, "CATEGORY_NOT_FOUND", "Category with the given id is not found"),
    EMPTY_CATEGORY_NAME(400, "EMPTY_CATEGORY_NAME", "Category name cannot be empty."),

    /**
     * Document
     **/
    DOCUMENT_NOT_FOUND(400, "DOCUMENT_NOT_FOUND", "Document not found."),
    DOCUMENT_SORT_OPTION_ERROR(400, "DOCUMENT_SORT_OPTION_ERROR", "Document sort option setting error"),

    /**
     * Outbox
     */
    OUTBOX_NOT_FOUND(400, "OUTBOX_NOT_FOUND", "Outbox not found."),

    /**
     * JWT Exceptions
     **/
    INVALID_JWT_TOKEN(401, "INVALID_JWT_TOKEN", "JWT token is invalid."),
    EXPIRED_JWT_TOKEN(401, "EXPIRED_JWT_TOKEN", "JWT token is expired."),

    /**
     * Global Exceptions
     **/
    UNAUTHORIZED_OPERATION_EXCEPTION(403, "UNAUTHORIZED_OPERATION_EXCEPTION", "You do not have permission to perform the operation"),

    /**
     * Subscription
     */
    FREE_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR(400, "FREE_PLAN_CURRENT_UPLOAD_ERROR", "무료 플랜으로 한달에 등록할 수 있는 문서의 최대 개수를 초과했습니다."),
    PRO_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR(400, "PRO_PLAN_CURRENT_UPLOAD_ERROR", "프로 플랜으로 한달에 등록할 수 있는 문서의 최대 개수를 초과했습니다."),
    FREE_PLAN_ANYTIME_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR(400, "FREE_PLAN_ANYTIME_UPLOAD_ERROR", "무료 플랜으로 매 시점에 등록할 수 있는 문서의 최대 개수를 초과했습니다."),
    PRO_PLAN_ANYTIME_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR(400, "PRO_PLAN_ANYTIME_UPLOAD_ERROR", "프로 플랜으로 매 시점에 등록할 수 있는 문서의 최대 개수를 초과했습니다."),
    FREE_PLAN_AI_PICK_LIMIT_EXCEED_ERROR(400, "FREE_PLAN_AI_PICK_LIMIT_EXCEED_ERROR", "무료 플랜으로 사용할 수 있는 AI Pick을 초과했습니다."),

    /**
     * KeyPoint
     */
    KEY_POINT_NOT_FOUND(400, "KEY_POINT_NOT_FOUND", "keypoint not found."),
    DEFAULT_FILE_NOT_FOUND(400, "DEFAULT_FILE_NOT_FOUND", "Default keypoint set not found."),



    /**
     * Event
     */
    EVENT_NOT_FOUND(400, "EVENT_NOT_FOUND", "Event not found."),
    POINT_NOT_ENOUGH(400, "POINT_NOT_ENOUGH", "Point not enough."),

    /**
     * Quiz
     */
    QUIZ_NOT_FOUND_ERROR(400, "QUIZ_NOT_FOUND", "Quiz set not found."),
    QUIZ_NOT_IN_DOCUMENT(400, "QUIZ_NOT_IN_DOCUMENT", "Quiz does not exist in this document."),
    QUIZ_SET_NOT_FOUND_ERROR(400, "QUIZ_SET_NOT_FOUND", "Quiz set not found.");



    private final int statusCode;
    private final String errorCode;
    private final String message;

    ErrorInfo(int statusCode, String errorCode, String message) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
    }
}

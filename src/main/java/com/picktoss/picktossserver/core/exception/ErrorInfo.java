package com.picktoss.picktossserver.core.exception;

import lombok.Getter;

@Getter
public enum ErrorInfo {
    /**
     * Authentication
     **/
    MEMBER_ALREADY_EXIST(400, "MEMBER_ALREADY_EXIST", "Member with the given email already exists."),
    MEMBER_NOT_FOUND(400, "MEMBER_NOT_FOUND", "Member with the given email does not exist."),

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

    /**
     * Question
     */
    QUESTION_SET_NOT_FOUND_ERROR(400, "QUESTION_SET_NOT_FOUND", "Question set not found.");



    private final int statusCode;
    private final String errorCode;
    private final String message;

    ErrorInfo(int statusCode, String errorCode, String message) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
    }
}

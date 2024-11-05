package com.picktoss.picktossserver.core.exception;

import lombok.Getter;

@Getter
public enum ErrorInfo {
    /**
     * Authentication
     **/
    MEMBER_NOT_FOUND(400, "MEMBER_NOT_FOUND", "Member with the given email does not exist."),
    EMAIL_VERIFICATION_NOT_FOUND(400, "EMAIL_VERIFICATION_NOT_FOUND", "Email verification with the given email is not found."),
    EMAIL_ALREADY_VERIFIED(400, "EMAIL_ALREADY_VERIFIED", "Email address is already verified."),
    EMAIL_NOT_VERIFIED(400, "EMAIL_NOT_VERIFIED", "Email is not verified."),
    INVALID_VERIFICATION_CODE(400, "INVALID_VERIFICATION_CODE", "Invalid verification code."),
    VERIFICATION_CODE_EXPIRED(400, "VERIFICATION_CODE_EXPIRED", "Verification code is expired"),

    /**
     * AWS Exception
     */
    AMAZON_SERVICE_EXCEPTION(400, "AMAZON_SERVICE_EXCEPTION", "Amazon service error."),
    FILE_UPLOAD_ERROR(400, "FILE_UPLOAD_ERROR", "There was a problem uploading the file."),

    /**
     * Directory
     **/
    DUPLICATE_DIRECTORY(400, "DUPLICATE_DIRECTORY", "Parent directory cannot have multiple child directories with the same name"),
    DIRECTORY_NOT_FOUND(400, "DIRECTORY_NOT_FOUND", "Directory with the given id is not found"),
    EMPTY_DIRECTORY_NAME(400, "EMPTY_DIRECTORY_NAME", "Directory name cannot be empty."),

    /**
     * Document
     **/
    DOCUMENT_NOT_FOUND(400, "DOCUMENT_NOT_FOUND", "Document not found."),
    DOCUMENT_SORT_OPTION_ERROR(400, "DOCUMENT_SORT_OPTION_ERROR", "Document sort option setting error"),
    DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR(400, "DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR", "등록할 수 있는 문서의 최대 개수를 초과했습니다."),

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
    UNABLE_TO_CONVERT_LIST_TO_STRING(400, "UNABLE_TO_CONVERT_LIST_TO_STRING", "UNABLE_TO_CONVERT_LIST_TO_STRING."),
    UNABLE_TO_CONVERT_STRING_TO_LIST(400, "UNABLE_TO_CONVERT_STRING_TO_LIST", "UNABLE_TO_CONVERT_STRING_TO_LIST"),

    /**
     * Collection
     */
    COLLECTION_NOT_FOUND(400, "COLLECTION_NOT_FOUND", "Collection not found."),
    DUPLICATE_QUIZ_IN_COLLECTION(400, "DUPLICATE_QUIZ_IN_COLLECTION", "해당 퀴즈는 이미 컬렉션에 포함되어 있습니다."),
    OWN_COLLECTION_CANT_BOOKMARK(400, "OWN_COLLECTION_CANT_BOOKMARK", "자신의 컬렉션은 북마크할 수 없습니다."),
    INTEREST_COLLECTION_FIELD_NOT_FOUND(400, "INTEREST_COLLECTION_FIELD_NOT_FOUND", "관심분야 설정이 되어있지 않습니다"),

    /**
     * Star
     */
    STAR_NOT_FOUND(400, "STAR_NOT_FOUND", "Star not found."),
    STAR_SHORTAGE_IN_POSSESSION(400, "STAR_SHORTAGE_IN_POSSESSION", "Star shortage in possession."),

    /**
     * Quiz
     */
    QUIZ_NOT_FOUND_ERROR(400, "QUIZ_NOT_FOUND", "Quiz set not found."),
    QUIZ_COUNT_EXCEEDED(400, "QUIZ_COUNT_EXCEEDED", "You have exceeded the number of quizzes you can generate."),
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

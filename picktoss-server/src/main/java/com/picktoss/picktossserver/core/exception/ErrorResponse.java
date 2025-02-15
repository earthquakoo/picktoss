package com.picktoss.picktossserver.core.exception;

public class ErrorResponse extends ErrorResponseDto {

    private ErrorResponse(ErrorInfo errorInfo) {
        super(errorInfo.getStatusCode(), errorInfo.getErrorCode(), errorInfo.getMessage());
    }

    public static ErrorResponseDto from(ErrorInfo errorInfo) {
        return new ErrorResponseDto(errorInfo.getStatusCode(), errorInfo.getErrorCode(), errorInfo.getMessage());
    }
}

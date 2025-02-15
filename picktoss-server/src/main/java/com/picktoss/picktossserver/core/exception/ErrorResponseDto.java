package com.picktoss.picktossserver.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {

    private final int statusCode;
    private final String errorCode;
    private final String message;
}

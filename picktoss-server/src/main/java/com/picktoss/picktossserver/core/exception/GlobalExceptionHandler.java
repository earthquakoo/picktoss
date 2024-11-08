package com.picktoss.picktossserver.core.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomException(CustomException e) {
        // Extract error info
        ErrorInfo errorInfo = e.getErrorInfo();

        // Build response body
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                errorInfo.getStatusCode(),
                errorInfo.getErrorCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(errorInfo.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponseDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");

        // Check for class-level constraint violations
        if (!e.getBindingResult().getGlobalErrors().isEmpty()) {
            String globalErrorMessage = e.getBindingResult().getGlobalErrors().get(0).getDefaultMessage();
            ErrorResponseDto errorResponseDto = new ErrorResponseDto(400, "INVALID_ARGUMENT", globalErrorMessage);
            return new ResponseEntity<>(errorResponseDto, headers, HttpStatus.BAD_REQUEST);
        }

        FieldError fieldError = e.getBindingResult().getFieldError();
        String errorMessage = "Method argument not valid";
        if (fieldError != null) {
            errorMessage = "'" + fieldError.getField() + "' " + fieldError.getDefaultMessage();
        }

        String messageCode = fieldError != null && fieldError.getCode() != null ? fieldError.getCode() : "";
        ErrorResponseDto errorResponseDto = getErrorResponse(messageCode, errorMessage);
        return new ResponseEntity<>(errorResponseDto, headers, BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(400, "INVALID_INPUT_FORMAT", e.getMessage());
        return new ResponseEntity<>(errorResponseDto, headers, BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException e) {
        System.out.println("e.getClass().getName() = " + e.getClass().getName());
        e.printStackTrace();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(500, "INTERNAL_ERROR", e.getMessage());
        return new ResponseEntity<>(errorResponseDto, headers, INTERNAL_SERVER_ERROR);
    }

    public ErrorResponseDto getErrorResponse(String originCode, String message) {
        return switch (originCode) {
            case "NotBlank" -> new ErrorResponseDto(BAD_REQUEST.value(), "EMPTY_FIELD", message);
            case "Email" -> new ErrorResponseDto(BAD_REQUEST.value(), "INVALID_EMAIL_FORMAT", message);
            case "Pattern" -> new ErrorResponseDto(BAD_REQUEST.value(), "INVALID_INPUT_FORMAT", message);
            case "Max", "Min", "Size" -> new ErrorResponseDto(BAD_REQUEST.value(), "INVALID_FIELD_SIZE", message);
            default -> new ErrorResponseDto(BAD_REQUEST.value(), "INVALID_ARGUMENT", message);
        };
    }

}

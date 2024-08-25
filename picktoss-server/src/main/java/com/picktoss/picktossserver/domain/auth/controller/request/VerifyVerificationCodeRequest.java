package com.picktoss.picktossserver.domain.auth.controller.request;

import lombok.Getter;

@Getter
public class VerifyVerificationCodeRequest {
    private String email;
    private String verificationCode;
}

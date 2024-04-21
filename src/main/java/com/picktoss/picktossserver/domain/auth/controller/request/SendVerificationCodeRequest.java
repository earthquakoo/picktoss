package com.picktoss.picktossserver.domain.auth.controller.request;

import lombok.Getter;

@Getter
public class SendVerificationCodeRequest {
    private Long memberId;
    private String email;
}

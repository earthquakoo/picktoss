package com.picktoss.picktossserver.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class SendVerificationCodeRequest {
    @Email
    private String email;
}

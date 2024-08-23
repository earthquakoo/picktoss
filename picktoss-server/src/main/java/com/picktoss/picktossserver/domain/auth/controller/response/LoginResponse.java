package com.picktoss.picktossserver.domain.auth.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private Date accessTokenExpiration;
    private boolean isSignUp;
}

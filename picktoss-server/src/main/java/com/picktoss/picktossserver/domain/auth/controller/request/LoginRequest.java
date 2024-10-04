package com.picktoss.picktossserver.domain.auth.controller.request;

import com.picktoss.picktossserver.global.enums.SocialPlatform;
import lombok.Getter;

@Getter
public class LoginRequest {
    private String accessToken;
    private SocialPlatform socialPlatform;
}

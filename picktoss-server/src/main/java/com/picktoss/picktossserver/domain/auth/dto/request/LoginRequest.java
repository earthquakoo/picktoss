package com.picktoss.picktossserver.domain.auth.dto.request;

import com.picktoss.picktossserver.global.enums.member.SocialPlatform;
import lombok.Getter;

@Getter
public class LoginRequest {
    private String accessToken;
    private SocialPlatform socialPlatform;
}

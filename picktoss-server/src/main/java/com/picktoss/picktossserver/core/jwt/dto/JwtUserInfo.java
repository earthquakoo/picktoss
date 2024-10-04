package com.picktoss.picktossserver.core.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtUserInfo {
    private final Long memberId;
    private final String role;
}

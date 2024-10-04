package com.picktoss.picktossserver.core.jwt.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class JwtTokenDto {

    private String accessToken;
    private Date accessTokenExpiration;
}

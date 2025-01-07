package com.picktoss.picktossserver.domain.auth.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoogleMemberDto {
    private String id;
    private String email;
    private String name;
}

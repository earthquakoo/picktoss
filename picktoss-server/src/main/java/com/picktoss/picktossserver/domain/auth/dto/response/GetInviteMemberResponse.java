package com.picktoss.picktossserver.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetInviteMemberResponse {
    private String name;
}

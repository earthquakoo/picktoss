package com.picktoss.picktossserver.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateInviteLinkResponse {
    private String inviteLink;
}

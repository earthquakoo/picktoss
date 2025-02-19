package com.picktoss.picktossserver.domain.auth.dto.response;

import com.picktoss.picktossserver.global.enums.auth.CheckInviteCodeResponseType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckInviteCodeBySignUpResponse {
    private CheckInviteCodeResponseType type;
}
